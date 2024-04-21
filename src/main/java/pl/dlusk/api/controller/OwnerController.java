package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.OwnerService;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@AllArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;
    private final RestaurantService restaurantService;
    private final FoodOrderService foodOrderService;


    @PostMapping("/registerOwner")
    public String registerOwner(@RequestParam("name") String name,
                                @RequestParam("surname") String surname,
                                @RequestParam("phoneNumber") String phoneNumber,
                                @RequestParam("nip") String nip,
                                @RequestParam("regon") String regon,
                                @RequestParam("user.username") String username,
                                @RequestParam("user.email") String email,
                                @RequestParam("user.password") String password,
                                @RequestParam("user.enabled") boolean enabled,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        Owner owner = createOwner(name, surname, phoneNumber, nip, regon, username, password, email, enabled);
        session.setAttribute("owner", owner);

        try {
            ownerService.registerOwner(owner, owner.getUser());
        } catch (UsernameAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nazwa użytkownika lub NIP właściciela już istnieje.");
            return "redirect:/registration";
        }

        return "redirect:/showOwnerLoggedInView";
    }

    @GetMapping("/showOwnerLoggedInView")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public String showOwnerLoggedInView(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            log.info("Session expired or user not logged in.");
            return "redirect:/login";
        }

        FoodOrderingAppUser user = ownerService.getUserByUsername(username);
        Restaurant restaurant = restaurantService.getRestaurantByUsername(username);

        session.setAttribute("user", user);
        session.setAttribute("restaurant", restaurant);

        if (restaurant == null) {
            log.info("Restaurant not found for username: {}", username);
            return "redirect:/showRestaurantRegistrationForm";
        }

        Menu menu = restaurantService.getMenuByRestaurant(restaurant);
        if (menu == null) {
            log.info("No menu found for restaurant ID: {}", restaurant.getRestaurantId());
            return "redirect:/showAddMenuToTheRestaurantView";
        }

        Set<MenuItem> menuItems = restaurantService.getMenuItemsByMenuId(menu.getMenuId());
        if (menuItems.isEmpty()) {
            log.info("No menu items found for menu ID: {}", menu.getMenuId());
            return "redirect:/addItemsToTheMenuView";
        }

        model.addAttribute("menu", menu.withMenuItems(menuItems));
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("ownerUsername", username);

        log.info("Owner logged in view displayed for {}", username);
        return "ownerRestaurantInfo";
    }



    @PostMapping("/updateFoodOrderStatusToDelivery/{orderId}")
    public String updateFoodOrderStatusToDelivery(@PathVariable Long orderId, HttpSession session) {
        foodOrderService.updateFoodOrderStatus(orderId, FoodOrderStatus.DELIVERED.toString());
        log.info("Order with id {} status updated to Delivery.", orderId);
        return "redirect:/showOrdersInProgress";
    }
    @GetMapping("/showFinishedOrders")
    public String showFinishedOrders(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        SecurityContext context = SecurityContextHolder.getContext();

        log.info("Fetching restaurant for user {}", username);
        Restaurant restaurant = restaurantService.getRestaurantByUsername(username);

        if (restaurant == null) {
            log.warn("No restaurant found for username: {}", username);
            return "redirect:/showRestaurantRegistrationForm";
        }

        List<FoodOrder> finishedFoodOrders = foodOrderService.getFoodOrdersWithStatus(restaurant.getRestaurantId()
        ,FoodOrderStatus.DELIVERED.toString());
        model.addAttribute("finishedFoodOrders", finishedFoodOrders);

        log.debug("Finished orders for restaurant ID {}: {}", restaurant.getRestaurantId(), finishedFoodOrders.size());
        return "finishedOrdersView";
    }

    @GetMapping("/showOrdersInProgress")
    public String showOrdersInProgress(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        log.debug("Fetching restaurant for user {}", username);
        Restaurant restaurant = restaurantService.getRestaurantByUsername(username);
        if (restaurant == null) {
            log.warn("No restaurant found for username: {}", username);
            return "redirect:/showRestaurantRegistrationForm";
        }

        List<FoodOrder> foodOrdersInProgress = foodOrderService.getFoodOrdersWithStatus(restaurant.getRestaurantId(), FoodOrderStatus.CONFIRMED.toString());

        List<FoodOrder> fooOrdersInProgressWithRestaurant = getFoodOrders(foodOrdersInProgress, restaurant);

        model.addAttribute("foodOrdersInProgress", fooOrdersInProgressWithRestaurant);
        log.info("########## OwnerController #### showOrdersInProgress #  FINISH WITH foodOrdersInProgress {}",
                fooOrdersInProgressWithRestaurant);
        return "foodOrdersForRestaurantInProgressView";
    }

    private List<FoodOrder> getFoodOrders(List<FoodOrder> foodOrdersInProgress, Restaurant restaurant) {
        List<FoodOrder> fooOrdersInProgressWithRestaurant = foodOrdersInProgress.stream().map(
                foodOrder -> {
                    Long foodOrderId = foodOrder.getFoodOrderId();
                    Set<OrderItem> orderItemsByFoodOrderId = foodOrderService.findOrderItemsByFoodOrderId(foodOrderId);
                    return foodOrder.withOrderItems(orderItemsByFoodOrderId)
                            .withRestaurant(restaurant);
                }
        ).toList();
        return fooOrdersInProgressWithRestaurant;
    }


    private Owner createOwner(String name, String surname, String phoneNumber, String nip, String regon,
                              String username, String password, String email, boolean enabled) {

        FoodOrderingAppUser user = createUser(username, password, email, enabled);

        return Owner.builder()
                .name(name)
                .surname(surname)
                .phoneNumber(phoneNumber)
                .nip(nip)
                .regon(regon)
                .user(user)
                .build();
    }

    private FoodOrderingAppUser createUser(String username, String password, String email, boolean enabled) {
        return FoodOrderingAppUser.builder()
                .username(username)
                .password(password)
                .email(email)
                .role(Roles.OWNER.toString())
                .enabled(enabled)
                .build();
    }
}
