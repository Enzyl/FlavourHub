package pl.dlusk.business;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserDAO;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class FoodOrderService {
    private final FoodOrderDAO foodOrderDAO;
    private final FoodOrderingAppUserDAO foodOrderingAppUserDAO;
    private final ClientDAO clientDAO;
    private final RestaurantDAO restaurantDAO;
    private final FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    @Transactional
    public FoodOrder createOrUpdateFoodOrder(FoodOrder foodOrder) {
        return foodOrderDAO.save(foodOrder);
    }

    public FoodOrder getFoodOrderById(Long id) {
        return foodOrderDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOrder with id [%s] not found".formatted(id)));
    }


    public List<FoodOrder> getAllFoodOrders() {
        // Logika do pobierania wszystkich zamówień
        return foodOrderDAO.findAll();
    }

    public List<FoodOrder> getFoodOrdersByClient(Long clientId) {
        // Logika do pobierania zamówień danego klienta
        return foodOrderDAO.findByClientId(clientId);
    }

    public List<FoodOrder> getFoodOrdersByRestaurant(Long restaurantId) {
        // Logika do pobierania zamówień dla danej restauracji
        return foodOrderDAO.findByRestaurantId(restaurantId);
    }

    public void deleteFoodOrder(Long id) {
        // Logika do usuwania zamówienia po ID
        foodOrderDAO.deleteById(id);
    }

    public List<FoodOrder> getFoodOrdersByStatus(String status) {
        // Logika do pobierania zamówień o określonym statusie
        return foodOrderDAO.findByOrderStatus(status);
    }

    public List<FoodOrder> getFoodOrdersWithinDateRange(LocalDateTime start, LocalDateTime end) {
        // Logika do pobierania zamówień w określonym przedziale czasowym
        return foodOrderDAO.findByDateRange(start, end);
    }

    public Review addReviewToRestaurant(Long orderId, Review review) {
        return foodOrderDAO.addReview(orderId,review);
    }
    public String createFoodOrder(Long restaurantId, String username, BigDecimal totalValue, Delivery delivery, Payment payment, ShoppingCart shoppingCart) {
        log.info("########## FoodOrderService #### createFoodOrder # START");
        Long clientId = foodOrderingAppUserRepository.findIdByUsername(username);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = LocalDateTime.now().format(formatter);

        // Parsowanie sformatowanego czasu z powrotem na LocalDateTime
        LocalDateTime paymentTime = LocalDateTime.parse(formattedDateTime, formatter);
        Client client = clientDAO.findByUserId(clientId);
        Restaurant restaurant = restaurantDAO.findRestaurantById(restaurantId);

        Map<MenuItem, Integer> items = shoppingCart.getItems();
        log.info("########## FoodOrderService #### createFoodOrder # items:" + items);

        Set<OrderItem> orderItems = new HashSet<>();
        for (Map.Entry<MenuItem, Integer> entry : items.entrySet()) {
            OrderItem newItem = OrderItem.builder().menuItem(entry.getKey()).quantity(entry.getValue()).build();
            orderItems.add(newItem);
            log.info("########## FoodOrderService #### createFoodOrder # orderItem added:" + newItem);
        }

        log.info("########## FoodOrderService #### createFoodOrder # final orderItems:" + orderItems);
        String uniqueOrderNumber = UUID.randomUUID().toString();
        FoodOrder foodOrder = FoodOrder.builder()
                .orderTime(paymentTime)
                .foodOrderStatus(FoodOrderStatus.CONFIRMED.toString())
                .totalPrice(totalValue)
                .orderNumber(uniqueOrderNumber)
                .build()
                .withClient(client)
                .withRestaurant(restaurant)
                .withDelivery(delivery)
                .withPayment(payment)
                .withOrderItems(orderItems);

        foodOrderDAO.save(foodOrder);
        return uniqueOrderNumber;
    }

    @Transactional
    public void updateFoodOrderStatus(Long orderId, String status) {
        log.info("########## FoodOrderService #### cancelOrder # START");

        // Aktualizacja statusu zamówienia na "Cancelled"
        foodOrderDAO.updateFoodOrderStatus(orderId, status);
        log.info("Order with id {} has been updated to status {}.", orderId, status);
    }

    public FoodOrder findFoodOrderByOrderNumber(String foodOrderNumber){
        log.info("########## FoodOrderService #### findFoodOrderByOrderNumber # START");
        FoodOrder foodOrder = foodOrderDAO.findFoodOrderByFoodOrderNumber(foodOrderNumber);

        Long foodOrderId = foodOrder.getFoodOrderId();
        Set<OrderItem> orderItemsByFoodOrderId = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderId);

        log.info("########## FoodOrderService #### findFoodOrderByOrderNumber # FINISHC");
        return foodOrder;
    }
    public FoodOrder showOrderSummary(String uniqueFoodNumber){

        FoodOrder foodOrderByOrderNumber = findFoodOrderByOrderNumber(uniqueFoodNumber);
        Set<OrderItem> orderItemsByFoodOrderId = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderByOrderNumber.getFoodOrderId());
        FoodOrder foodOrderWithOrderItems = foodOrderByOrderNumber.withOrderItems(orderItemsByFoodOrderId);
        return foodOrderWithOrderItems;
    }
    public Set<OrderItem> findOrderItemsByFoodOrderId(Long foodOrderId){
        Set<OrderItem> orderItemsByFoodOrderId = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderId);
        return orderItemsByFoodOrderId;
    }

    public List<FoodOrder> getFoodOrdersWithStatus(Long restaurantId, String status) {
        List<FoodOrder> foodOrdersForRestaurant = foodOrderDAO.findByRestaurantId(restaurantId);
        return foodOrdersForRestaurant.stream()
                .filter(order -> status.equals(order.getFoodOrderStatus()))
                .map(this::enrichFoodOrderWithDetails)
                .collect(Collectors.toList());
    }

    private FoodOrder enrichFoodOrderWithDetails(FoodOrder foodOrder) {
        Set<OrderItem> orderItems = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrder.getFoodOrderId());
        return foodOrder.withOrderItems(orderItems);
    }


    @Transactional
    public String processOrder(HttpSession session) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
        Long restaurantId = (Long) session.getAttribute("restaurantId");
        FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
        BigDecimal totalValue = (BigDecimal) session.getAttribute("totalValue");
        Delivery delivery = (Delivery) session.getAttribute("delivery");
        Payment payment = (Payment) session.getAttribute("payment");

        String uniqueFoodNumber = createFoodOrder(restaurantId, user.getUsername(), totalValue, delivery, payment, shoppingCart);

        clearOrderSessionAttributes(session, uniqueFoodNumber);

        return uniqueFoodNumber;
    }

    private void clearOrderSessionAttributes(HttpSession session, String uniqueFoodNumber) {
        session.removeAttribute("delivery");
        session.removeAttribute("shoppingCart");
        session.removeAttribute("payment");
        session.removeAttribute("totalValue");
        session.removeAttribute("restaurantId");
        session.removeAttribute("location");
        session.removeAttribute("uniqueFoodNumber");
        session.setAttribute("uniqueFoodNumber", uniqueFoodNumber);
    }

}
