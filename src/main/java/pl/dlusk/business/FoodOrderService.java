package pl.dlusk.business;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.business.dao.PaymentDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.User;
import pl.dlusk.infrastructure.security.UserRepository;

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
    private final ClientDAO clientDAO;
    private final PaymentDAO paymentDAO;
    private final RestaurantDAO restaurantDAO;
    private final UserRepository foodOrderingAppUserRepository;
    private final UtilService utilService;

    public FoodOrder getFoodOrderById(Long id) {
        return foodOrderDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOrder with id [%s] not found".formatted(id)));
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

        // Aktualizacja statusu zamówienia na "Cancelled"
        foodOrderDAO.updateFoodOrderStatus(orderId, status);
        log.debug("Order with id {} has been updated to status {}.", orderId, status);
    }

    public FoodOrder showOrderSummary(String uniqueFoodNumber) {
        // This method now trusts that findFoodOrderByOrderNumber properly attaches the order items
        return findFoodOrderByOrderNumber(uniqueFoodNumber);
    }

    public FoodOrder findFoodOrderByOrderNumber(String foodOrderNumber) {
        FoodOrder foodOrder = foodOrderDAO.findFoodOrderByFoodOrderNumber(foodOrderNumber);
        if (foodOrder != null) {
            Set<OrderItem> orderItems = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrder.getFoodOrderId());
            foodOrder.withOrderItems(orderItems);
        }
        return foodOrder;
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
        User user = (User) session.getAttribute("user");
        BigDecimal totalValue = (BigDecimal) session.getAttribute("totalValue");
        Delivery delivery = (Delivery) session.getAttribute("delivery");
        Payment payment = (Payment) session.getAttribute("payment");

        String uniqueFoodNumber = createFoodOrder(restaurantId, user.getUsername(), totalValue, delivery, payment, shoppingCart);

        utilService.clearOrderSessionAttributes(session, uniqueFoodNumber);

        return uniqueFoodNumber;
    }
    public ClientOrderHistory.FoodOrderRequest convertToFoodOrderRequest(FoodOrder foodOrder, Set<OrderItem> orderItems) {
        Long foodOrderId = foodOrder.getFoodOrderId();
        Restaurant restaurantByFoodOrderId = restaurantDAO.findRestaurantByFoodOrderId(foodOrderId);
        Payment paymentByFoodOrderId = paymentDAO.findByFoodOrderId(foodOrderId);

        return ClientOrderHistory.FoodOrderRequest.builder()
                .orderId(foodOrder.getFoodOrderId())
                .orderTime(foodOrder.getOrderTime())
                .foodOrderStatus(foodOrder.getFoodOrderStatus())
                .totalPrice(foodOrder.getTotalPrice())
                .restaurant(restaurantByFoodOrderId)
                .orderItems(orderItems)
                .payment(paymentByFoodOrderId)
                .build();
    }
    public List<FoodOrder> getFoodOrders(List<FoodOrder> foodOrdersInProgress, Restaurant restaurant) {
        List<FoodOrder> fooOrdersInProgressWithRestaurant = foodOrdersInProgress.stream().map(
                foodOrder -> {
                    Long foodOrderId = foodOrder.getFoodOrderId();
                    Set<OrderItem> orderItemsByFoodOrderId = findOrderItemsByFoodOrderId(foodOrderId);
                    return foodOrder.withOrderItems(orderItemsByFoodOrderId)
                            .withRestaurant(restaurant);
                }
        ).toList();
        return fooOrdersInProgressWithRestaurant;
    }
}
