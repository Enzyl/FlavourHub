package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.OwnerService;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.Enumeration;
import java.util.List;
import java.util.Set;

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
                .password(password)
                .email(email)
                .role(Roles.OWNER.toString())
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

        return "redirect:/showOwnerLoggedInView";
    }

    @GetMapping("/showOwnerLoggedInView")
    public String showOwnerLoggedInView(HttpSession session, Model model) {

        String username = (String) session.getAttribute("username");
        log.info("########## OwnerController #### showOwnerLoggedInView #  username   " + username);

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

        log.info("########## OwnerController #### showOrdersInProgress # restaurantId {}", restaurantId);
        List<FoodOrder> foodOrdersForRestaurant = foodOrderService.getFoodOrdersByRestaurant(restaurantId);

        log.info("########## OwnerController #### showOrdersInProgress # foodOrdersForRestaurant {}",
                foodOrdersForRestaurant);

        List<FoodOrder> foodOrdersInProgress = foodOrdersForRestaurant
                .stream().filter(a -> a.getFoodOrderStatus().equals(FoodOrderStatus.CONFIRMED.toString())).toList();
        Restaurant restaurant = restaurantDAO.findRestaurantById(restaurantId);

        List<FoodOrder> fooOrdersInProgressWithRestaurant = foodOrdersInProgress.stream().map(
                foodOrder -> {
                    Long foodOrderId = foodOrder.getFoodOrderId();
                    Set<OrderItem> orderItemsByFoodOrderId = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderId);
                    return foodOrder.withOrderItems(orderItemsByFoodOrderId)
                            .withRestaurant(restaurant);
                }
        ).toList();

        model.addAttribute("foodOrdersInProgress", fooOrdersInProgressWithRestaurant);
        log.info("########## OwnerController #### showOrdersInProgress #  FINISH WITH foodOrdersInProgress {}",
                fooOrdersInProgressWithRestaurant);
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
