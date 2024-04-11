package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.model.IModel;
import pl.dlusk.business.ClientService;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import pl.dlusk.infrastructure.database.repository.jpa.OrderItemsJpaRepository;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserDAO;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Controller
@AllArgsConstructor
public class ClientController {

    private final ClientService clientService;
    private final FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    private final FoodOrderService foodOrderService;
    private final FoodOrderingAppUserDAO foodOrderingAppUserDAO;
    private final FoodOrderDAO foodOrderDAO;
    private final RestaurantDAO restaurantDAO;
    private final OrderItemsJpaRepository orderItemsJpaRepository;


    @GetMapping("/registerClient")
    public String showRegistrationForm(Model model) {

        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username("")
                .password("")
                .email("")
                .role("ROLE_USER")
                .enabled(true)
                .build();


        Client client = Client.builder()
                .fullName("")
                .phoneNumber("")
                .user(user)
                .build();
        model.addAttribute("client", client);
        return "registerClient";
    }


    @PostMapping("/registerClient")
    public String registerClient(@RequestParam("fullName") String fullName,
                                 @RequestParam("phoneNumber") String phoneNumber,
                                 @RequestParam("user.username") String username,
                                 @RequestParam("user.password") String password,
                                 @RequestParam("user.role") String role,
                                 @RequestParam("user.enabled") boolean enabled,
                                 @RequestParam("user.email") String email,
                                 Model model) {
        log.info("########## ClientController #### registerClient #  START");
        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username(username)
                .password(password)
                .email(email)
                .role("CLIENT")
                .enabled(enabled)
                .build();
        log.info("########## ClientController #### registerClient #  user  " + user.toString());

        Client client = Client.builder()
                .phoneNumber(phoneNumber)
                .fullName(fullName)
                .user(user)
                .build();
        log.info("########## ClientController #### registerClient #  client" + client.toString());

        Client registeredClient = clientService.registerClient(client, user);
        log.info("########## ClientController #### registerClient #  registeredClient" + registeredClient.toString());

        model.addAttribute("registeredClient", registeredClient);


        return "registrationSuccessView";
    }

    @GetMapping("/clientLoggedInView")
    public String showClientLoggedInView(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        FoodOrderingAppUser user = foodOrderingAppUserRepository.findByUsername(username);
        session.setAttribute("user", user);

        model.addAttribute("username", user.getUsername());
        return "clientLoggedInView";
    }

    @GetMapping("/userProfileView")
    public String showUserProfileView(Model model, HttpSession session) {
        FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");

        String username = user.getUsername();
        log.info("########## ClientController #### showUserProfileView #  username: " + username);
        Client clientByUsername = clientService.getClientByUsername(username);
        log.info("########## ClientController #### showUserProfileView #  user: " + user);
        log.info("########## ClientController #### showUserProfileView #  client: " + clientByUsername.toString());
        model.addAttribute("user", user);
        model.addAttribute("client", clientByUsername);
        return "clientDetails";
    }

    @GetMapping("/deliveryAddress")
    public String showAddressForm() {
        log.info("########## ClientController #### showAddressForm #  was executed");
        return "deliveryAddressView";
    }


    @PostMapping("/submitDeliveryAddress")
    public String submitDeliveryAddress(
            @RequestParam("streetName") String streetName,
            @RequestParam("buildingNumber") String buildingNumber,
            @RequestParam("apartmentNumber") String apartmentNumber,
            @RequestParam("postalCode") String postalCode,
            @RequestParam("city") String city,
            @RequestParam(value = "additionalInstructions", required = false) String additionalInstructions,
            HttpSession session) {
        log.info("########## ClientController #### submitDeliveryAddress #  START");

        String deliveryAddress = String.format("%s %s, Apt. %s, %s %s, %s",
                streetName, buildingNumber, apartmentNumber.isEmpty() ? "N/A" : apartmentNumber, postalCode, city, additionalInstructions == null ? "" : additionalInstructions);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = LocalDateTime.now().format(formatter);

        LocalDateTime paymentTime = LocalDateTime.parse(formattedDateTime, formatter);
        Delivery delivery = Delivery.builder()
                .deliveryAddress(deliveryAddress)
                .deliveryStatus("W trakcie realizacji")
                .build();

        session.setAttribute("delivery", delivery);

        log.info("Delivery address submitted: {}", deliveryAddress);

        return "redirect:/confirmationPage";
    }

    @GetMapping("/confirmationPage")
    public String showConfirmationPage() {
        log.info("########## ClientController #### showConfirmationPage #  was executed");
        return "confirmationPage";
    }


    @GetMapping("/processOrder")
    public String processOrder(HttpSession session) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
        log.info("########## ClientController #### processOrder #  shoppingCart: " + shoppingCart.toString());

        Long restaurantId = (Long) session.getAttribute("restaurantId");
        FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
        Long clientId = foodOrderingAppUserRepository.findIdByUsername(user.getUsername());
        BigDecimal totalValue = (BigDecimal) session.getAttribute("totalValue");

        Delivery delivery = (Delivery) session.getAttribute("delivery");
        Payment payment = (Payment) session.getAttribute("payment");

        log.info("########## ClientController #### processOrder #  shoppingCart {}", shoppingCart);

        String uniqueFoodNumber = foodOrderService.createFoodOrder(
                restaurantId, clientId, totalValue, delivery, payment, shoppingCart);
        log.info("########## ClientController #### processOrder #  foodOrder saved");

        session.removeAttribute("delivery");
        session.removeAttribute("shoppingCart");
        session.removeAttribute("payment");
        session.removeAttribute("totalValue");
        session.removeAttribute("restaurantId");
        session.setAttribute("uniqueFoodNumber",uniqueFoodNumber);

        return "redirect:/showOrderSummary";
    }

    @GetMapping("/showOrderSummary")
    public String showOrderSummary(HttpSession session, Model model) {
        String uniqueFoodNumber = (String) session.getAttribute("uniqueFoodNumber");
        FoodOrder foodOrderByOrderNumber = foodOrderService.findFoodOrderByOrderNumber(uniqueFoodNumber);

        Set<OrderItem> orderItemsByFoodOrderId = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderByOrderNumber.getFoodOrderId());
        FoodOrder foodOrderWithOrderItems = foodOrderByOrderNumber.withOrderItems(orderItemsByFoodOrderId);

        model.addAttribute("foodOrderWithOrderItems",foodOrderWithOrderItems);
        return "orderSummaryView";
    }

    @GetMapping("/sessionAttributes")
    public String showSessionAttributes(HttpSession session, Model model) {
        Map<String, Object> attributes = new HashMap<>();
        session.getAttributeNames().asIterator().forEachRemaining(attributeName ->
                attributes.put(attributeName, session.getAttribute(attributeName))
        );
        model.addAttribute("attributes", attributes);
        return "sessionAttributes";
    }

    @GetMapping("/userOrders")
    public String showClientOrders(HttpSession session, Model model) {
        FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
        Long clientId = foodOrderingAppUserDAO.findIdByUsername(user.getUsername());
        ClientOrderHistory clientOrderHistory = clientService.getClientOrderHistory(clientId);

        log.info("########## ClientController #### showClientOrders #  clientOrderHistory: " + clientOrderHistory);
        model.addAttribute("clientOrderHistory", clientOrderHistory);
        return "userOrders";
    }

    @PostMapping("/cancelOrder")
    public String cancelOrder(@RequestParam("orderId") Long orderId, HttpSession session, RedirectAttributes redirectAttributes) {
        log.info("########## ClientController #### cancelOrder #  START ");
        FoodOrder foodOrder = foodOrderService.getFoodOrderById(orderId);
        log.info("########## ClientController #### cancelOrder #  foodOrder.getOrderTime(): " + foodOrder.getOrderTime());
        log.info("########## ClientController #### cancelOrder #  Duration.between(foodOrder.getOrderTime(), LocalDateTime.now()).toMinutes(): " + Duration.between(foodOrder.getOrderTime(), LocalDateTime.now()).toMinutes());


        if (Duration.between(foodOrder.getOrderTime(), LocalDateTime.now()).toMinutes() <= 20) {
            foodOrderService.updateFoodOrderStatus(orderId, "Cancelled");
            redirectAttributes.addFlashAttribute("successMessage", "Order has been cancelled successfully.");
            log.info("########## ClientController #### cancelOrder #  Order with ID [" + orderId + "] HAS BEEN CANCELED");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Order cannot be cancelled after 20 minutes.");
        }
        log.info("########## ClientController #### cancelOrder #  FINISH ");

        return "redirect:/userOrders";
    }
}