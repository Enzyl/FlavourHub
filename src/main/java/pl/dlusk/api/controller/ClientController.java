package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.api.dto.DeliveryAddressFormDTO;
import pl.dlusk.business.ClientService;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.UserService;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Controller
@AllArgsConstructor
public class ClientController {

    private final ClientService clientService;
     private final FoodOrderService foodOrderService;
     private final UserService userService;

    @PostMapping("/registerClient")
    public String registerClient(@RequestParam Map<String, String> params, RedirectAttributes redirectAttributes) {
        try {
            log.info("Starting client registration");
            FoodOrderingAppUser user = userService.createUserFromParams(params);
            Client client = clientService.createClientFromParams(params, user);

            Client registeredClient = clientService.registerClient(client, user);
            redirectAttributes.addFlashAttribute("registeredClient", registeredClient);
            log.info("Client registration successful: {}", registeredClient);
            return "redirect:/registrationSuccessView";
        } catch (UsernameAlreadyExistsException e) {
            log.error("Registration failed: Username or email already exists", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Username or email already exists.");
            return "redirect:/registerClientForm";
        } catch (Exception e) {
            log.error("Registration failed for user: {}", params.get("user.username"), e);
            redirectAttributes.addFlashAttribute("errorMessage", "Registration failed.");
            return "redirect:/registerClientForm";
        }
    }

    @GetMapping("/clientLoggedInView")
    public String showClientLoggedInView(Model model, HttpSession session) {
        String username = (String) session.getAttribute("username");
        FoodOrderingAppUser user = clientService.getUserByUsername(username);
        session.setAttribute("user", user);
        model.addAttribute("username", user.getUsername());
        return "clientLoggedInView";
    }

    @GetMapping("/userProfileView")
    public String showUserProfileView(Model model, Authentication authentication) {
        FoodOrderingAppUser user = (FoodOrderingAppUser) authentication.getPrincipal();

        if (user == null) {
            log.info("No authenticated user found.");
            return "redirect:/login";
        }

        log.debug("Displaying user profile for username: {}", user.getUsername());
        Client client = clientService.getClientByUsername(user.getUsername());

        if (client == null) {
            log.info("No client found for username: {}", user.getUsername());
            model.addAttribute("errorMessage", "No client profile available.");
        } else {
            model.addAttribute("user", user);
            model.addAttribute("client", client);
            log.debug("User and client profiles loaded for username: {}", user.getUsername());
        }

        return "clientDetails";
    }


    @GetMapping("/deliveryAddress")
    public String showAddressForm() {
        return "deliveryAddressView";
    }


    @PostMapping("/submitDeliveryAddress")
    public String submitDeliveryAddress(@ModelAttribute DeliveryAddressFormDTO form, BindingResult bindingResult, HttpSession session) {
        log.info("Submitting delivery address");
        if (bindingResult.hasErrors()) {
            return "deliveryAddressView";
        }
        String apartmentInfo = Optional.ofNullable(form.getApartmentNumber()).orElse("N/A");
        String additionalInfo = Optional.ofNullable(form.getAdditionalInstructions()).orElse("");

        String deliveryAddress = String.format("%s %s, Apt. %s, %s %s, %s",
                form.getStreetName(), form.getBuildingNumber(), apartmentInfo, form.getPostalCode(), form.getCity(), additionalInfo);


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
    public String processOrder(HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            String uniqueFoodNumber = foodOrderService.processOrder(session);
            redirectAttributes.addFlashAttribute("successMessage", "Order processed successfully!");
            return "redirect:/showOrderSummary";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error processing order: " + e.getMessage());
            return "redirect:/orderFailed";
        }
    }


    @GetMapping("/showOrderSummary")
    public String showOrderSummary(HttpSession session, Model model) {
        String uniqueFoodNumber = (String) session.getAttribute("uniqueFoodNumber");
        if (uniqueFoodNumber == null) {
            model.addAttribute("errorMessage", "No order found to display.");
            return "errorPage";  // Redirect to a generic error page or order not found page
        }

        FoodOrder foodOrder = foodOrderService.showOrderSummary(uniqueFoodNumber);
        if (foodOrder == null) {
            model.addAttribute("errorMessage", "Order details could not be retrieved.");
            return "errorPage";
        }

        model.addAttribute("foodOrderWithOrderItems", foodOrder);
        return "orderSummaryView";
    }


    @GetMapping("/userOrders")
    public String showClientOrders(HttpSession session, Model model) {
        FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
        if (user == null) {
            model.addAttribute("errorMessage", "User not found. Please login again.");
            return "loginView";
        }

        String username = user.getUsername();
        try {
            ClientOrderHistory clientOrderHistory = clientService.getClientOrderHistory(username);
            model.addAttribute("clientOrderHistory", clientOrderHistory);
            return "userOrders";
        } catch (Exception e) {
            log.error("Error retrieving orders for user {}: {}", username, e.getMessage());
            model.addAttribute("errorMessage", "Unable to retrieve orders at this time.");
            return "errorPage";
        }
    }


    @PostMapping("/cancelOrder")
    public String cancelOrder(@RequestParam("orderId") Long orderId, RedirectAttributes redirectAttributes) {
        log.info("########## ClientController #### cancelOrder #  START ");
        FoodOrder foodOrder = foodOrderService.getFoodOrderById(orderId);

        if (Duration.between(foodOrder.getOrderTime(), LocalDateTime.now()).toMinutes() <= 20) {
            foodOrderService.updateFoodOrderStatus(orderId, FoodOrderStatus.CANCELLED.toString());
            redirectAttributes.addFlashAttribute("successMessage", "Order has been cancelled successfully.");
            log.info("########## ClientController #### cancelOrder #  Order with ID [" + orderId + "] HAS BEEN CANCELED");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Order cannot be cancelled after 20 minutes.");
        }
        log.info("########## ClientController #### cancelOrder #  FINISH ");

        return "redirect:/userOrders";
    }

}
