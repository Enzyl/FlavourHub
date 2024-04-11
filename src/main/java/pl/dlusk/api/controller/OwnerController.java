package pl.dlusk.api.controller;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.OwnerService;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.repository.OwnerRepository;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Controller
@AllArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;
    private final RestaurantService restaurantService;
    private final RestaurantDAO restaurantDAO;
    private final FoodOrderService foodOrderService;
    private final FoodOrderDAO foodOrderDAO;
    private final FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    private final OwnerDAO ownerDAO;

    @GetMapping("/registerOwner")
    public String showOwnerRegistrationForm(Model model) {
        Owner owner = Owner.builder()
                .name("")
                .surname("")
                .phoneNumber("")
                .nip("")
                .regon("")
                .user(FoodOrderingAppUser.builder()
                        .username("")
                        .password("")
                        .email("")
                        .role("ROLE_OWNER") // Domyślna rola dla właściciela
                        .enabled(true) // Domyślnie aktywny
                        .build())
                .build();
        model.addAttribute("owner", owner);
        return "registerOwner"; // nazwa pliku HTML Thymeleaf z formularzem rejestracji właściciela
    }

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
                                Model model) {
        log.info("########## ClientController #### registerClient #  START");
        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username(username)
                .password(password) // Zakładam, że hasło jest już zakodowane lub zostanie zakodowane później
                .email(email)
                .role("OWNER")
                .enabled(enabled)
                .build();

        Owner owner = Owner.builder()
                .name(name)
                .surname(surname)
                .regon(regon)
                .nip(nip)
                .phoneNumber(phoneNumber)
                .user(user)
                .build();
        session.setAttribute("owner",owner);
        try {
            ownerService.registerOwner(owner, owner.getUser());
            log.info("########## OwnerController #### OWNERSAVED WITH:  #  USERNAME {} AND PASSWORD {}", username,password);
        } catch (UsernameAlreadyExistsException e) {
            model.addAttribute("errorMessage", "Nazwa użytkownika lub NIP właściciela już istnieje.");
            return "registrationSuccessView";
        }

        return "redirect:/showOwnerLoggedInView"; // przekierowanie do pulpitu właściciela po pomyślnej rejestracji
    }

    @GetMapping("/showOwnerLoggedInView")
    public String showOwnerLoggedInView(HttpSession session, Model model) {
        // Pobierz nazwę użytkownika zalogowanego właściciela
        String username = (String) session.getAttribute("username");
        log.info("########## OwnerController #### showOwnerLoggedInView #  username   " + username);

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            log.info("Session attribute - Name: {}, Value: {}", attributeName, attributeValue);
        }

        FoodOrderingAppUser user = foodOrderingAppUserRepository.findByUsername(username);
        session.setAttribute("user", user);
        Restaurant restaurantByUsername = restaurantService.getRestaurantByUsername(username);
        log.info("########## OwnerController #### showOwnerLoggedInView #  restaurantByUsername   " + restaurantByUsername);

        Owner owner = ownerDAO.findByUsername(username);
        log.info("########## OwnerController #### showOwnerLoggedInView #  owner   " + owner);

        if (restaurantByUsername == null) {
            log.info("########## OwnerController #### showOwnerLoggedInView #  restaurantByUsername   " + restaurantByUsername);
            return "redirect:/showRestaurantRegistrationForm";
        }
        Menu menu = restaurantService.getMenuByRestaurantId(restaurantByUsername.getRestaurantId());

        if (menu == null){
            return "redirect:/showAddMenuToTheRestaurantView";
        }

        Set<MenuItem> menuItemsByMenuId = restaurantDAO.findMenuItemsByMenuId(menu.getMenuId());

        if (menuItemsByMenuId.isEmpty()){
            return "redirect:/addItemsToTheMenuView";
        }
        menu = menu.withMenuItems(menuItemsByMenuId);

        model.addAttribute("menu", menu);
        model.addAttribute("restaurant", restaurantByUsername);
        model.addAttribute("ownerUsername", username);
        session.setAttribute("restaurant", restaurantByUsername);
        log.info("########## OwnerController #### showOwnerLoggedInView #  FINISHED");



        return "ownerRestaurantInfo";
    }

    @GetMapping("/showOrdersInProgress")
    public String showOrdersInProgress(HttpSession session, Model model) {
        log.info("########## OwnerController #### showOrdersInProgress #  START");
        String username = (String) session.getAttribute("username");
        Restaurant restaurantByUsername = restaurantService.getRestaurantByUsername(username);
        Long restaurantId = restaurantByUsername.getRestaurantId();

        log.info("########## OwnerController #### showOrdersInProgress #  FINISH WITH restaurantId {}", restaurantId);
        List<FoodOrder> foodOrdersForRestaurant = foodOrderService.getFoodOrdersByRestaurant(restaurantId);

        log.info("########## OwnerController #### showOrdersInProgress #  FINISH WITH foodOrdersForRestaurant {}",
                foodOrdersForRestaurant);

        List<FoodOrder> foodOrdersInProgress = foodOrdersForRestaurant
                .stream().filter(a -> a.getFoodOrderStatus().equals("Confirmed")).toList();
        Restaurant restaurant = restaurantDAO.findRestaurantById(restaurantId);


        List<FoodOrder> fooOrdeersInProgresWithRestaurant = foodOrdersInProgress.stream().map(
                foodOrder -> {
                    Long foodOrderId = foodOrder.getFoodOrderId();
                    Set<OrderItem> orderItemsByFoodOrderId = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderId);
                    return foodOrder.withOrderItems(orderItemsByFoodOrderId)
                            .withRestaurant(restaurant);
                }
        ).toList();

        model.addAttribute("foodOrdersInProgress", fooOrdeersInProgresWithRestaurant);
        log.info("########## OwnerController #### showOrdersInProgress #  FINISH WITH foodOrdersInProgress {}",
                fooOrdeersInProgresWithRestaurant);
        return "foodOrdersForRestaurantInProgressView";
    }

    @PostMapping("/updateFoodOrderStatusToDelivery/{orderId}")
    public String updateFoodOrderStatusToDelivery(@PathVariable Long orderId, HttpSession session) {
        foodOrderService.updateFoodOrderStatus(orderId, "Delivery");
        log.info("Order with id {} status updated to Delivery.", orderId);
        return "redirect:/showOrdersInProgress";
    }

    @GetMapping("/showFinishedOrders")
    public String showFinishedOrders(HttpSession session, Model model) {
        log.info("########## OwnerController #### showFinishedOrders #  START");
        String username = (String) session.getAttribute("username");
        Restaurant restaurantByUsername = restaurantService.getRestaurantByUsername(username);
        Long restaurantId = restaurantByUsername.getRestaurantId();

        log.info("########## OwnerController #### showFinishedOrders #  FINISH WITH restaurantId {}", restaurantId);
        List<FoodOrder> foodOrdersForRestaurant = foodOrderService.getFoodOrdersByRestaurant(restaurantId);

        log.info("########## OwnerController #### showFinishedOrders #  FINISH WITH foodOrdersForRestaurant {}",
                foodOrdersForRestaurant);

        List<FoodOrder> finishedFoodOrders = foodOrdersForRestaurant
                .stream().filter(a -> a.getFoodOrderStatus().equals("DELIVERED")).toList();

        List<FoodOrder> fooOrdersInProgressWithRestaurant = finishedFoodOrders.stream().map(
                foodOrder -> {
                    Long foodOrderId = foodOrder.getFoodOrderId();
                    Set<OrderItem> orderItemsByFoodOrderId = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderId);
                    return foodOrder.withOrderItems(orderItemsByFoodOrderId)
                            .withRestaurant(restaurantByUsername);
                }
        ).toList();
        model.addAttribute("fooOrdersInProgressWithRestaurant", fooOrdersInProgressWithRestaurant);
        log.info("########## OwnerController #### showFinishedOrders #  FINISH WITH finishedFoodOrders {}",
                fooOrdersInProgressWithRestaurant);
        return "finishedOrders";
    }

}
