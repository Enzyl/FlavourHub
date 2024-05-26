package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.api.dto.DeliveryStreetDTO;
import pl.dlusk.api.dto.MenuDTO;
import pl.dlusk.api.dto.MenuItemDTO;
import pl.dlusk.api.dto.RestaurantRegistrationDTO;
import pl.dlusk.api.dto.mapper.MenuDTOMapper;
import pl.dlusk.api.dto.mapper.MenuItemDTOMapper;
import pl.dlusk.business.*;
import pl.dlusk.domain.*;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.User;

import java.io.IOException;
import java.util.List;
import java.util.Set;

@Slf4j
@Controller
@AllArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final CloudinaryService cloudinaryService;
    private final OwnerService ownerService;
    private final PaymentService paymentService;
    private final ShoppingCartService shoppingCartService;
    private final FoodOrderService foodOrderService;
    private final MenuItemDTOMapper menuItemDTOMapper;
    private final MenuDTOMapper menuDTOMapper;
    private final UtilService utilService;

    @GetMapping("/restaurantMenu/{restaurantId}")
    public String showRestaurantMenu(@PathVariable Long restaurantId, Model model, HttpSession session) {
        log.info("Displaying menu for restaurant ID: {}", restaurantId);
        String username = (String) session.getAttribute("username");

        ShoppingCart shoppingCart = shoppingCartService.ensureShoppingCart(session, restaurantId, username);

        try {
            Menu menu = restaurantService.getMenuForRestaurantWithMenuItems(restaurantId);
            log.info("menu found from restaurantService.getMenuForRestaurantWithMenuItems(restaurantId): {}", menu);
            utilService.updateModelWithMenuDetails(model, shoppingCart, restaurantId, menu, session);
            log.info("model, shoppingCart, restaurantId, menu, session: {} {} {} {} {}", model, shoppingCart, restaurantId, menu, session);
            return "restaurantMenu";
        } catch (Exception e) {
            log.error("Failed to load menu for restaurant ID {}: {}", restaurantId, e.getMessage());
            model.addAttribute("errorMessage", "Menu for restaurant ID " + restaurantId + " could not be found.");
            return "errorPage";
        }
    }

    @PostMapping("/addToCart")
    public String addToCart(@RequestParam("menuItemId") Long menuItemId, HttpSession session, RedirectAttributes redirectAttributes) {

        ShoppingCart shoppingCart = shoppingCartService.getOrCreateShoppingCart(session);
        try {
            MenuItem menuItem = restaurantService.getMenuItemById(menuItemId);
            shoppingCart.addItem(menuItem);
            session.setAttribute("shoppingCart", shoppingCart);
            redirectAttributes.addFlashAttribute("successMessage", "Item added to cart successfully.");
        } catch (Exception e) {
            log.error("Error adding item to cart: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add item to cart.");
        }

        return "redirect:/restaurantMenu/" + shoppingCart.getRestaurantId();
    }


    @PostMapping("/updateCartItem")
    public String updateCartItem(@RequestParam("menuItemId") Long menuItemId, @RequestParam("quantity") int quantity, HttpSession session) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            // If no cart found, redirect to an error page or home page with a flash message
            return "errorPage"; // Replace "someErrorPage" with the actual error handling page
        }

        MenuItem menuItem = restaurantService.getMenuItemById(menuItemId);

        if (quantity > 0) {
            shoppingCart.updateItemQuantity(menuItem, quantity);
        } else {
            shoppingCart.removeItem(menuItem);
        }

        session.setAttribute("shoppingCart", shoppingCart);
        return "redirect:/restaurantMenu/" + shoppingCart.getRestaurantId();
    }

    @GetMapping("/showRestaurantRegistrationForm")
    public String showRegisterRestaurantForm(Model model) {
        RestaurantRegistrationDTO restaurantDTO = new RestaurantRegistrationDTO();
        restaurantDTO.setAddress(new RestaurantRegistrationDTO.AddressDTO());
        model.addAttribute("restaurant", restaurantDTO);
        return "restaurantRegistrationView";
    }
// TODO: Write test for this method but later, i get error with CloudinaryService
    @PostMapping("/registerRestaurant")
    public String registerRestaurant(
            @Valid @ModelAttribute("restaurant") RestaurantRegistrationDTO restaurantDTO,
            BindingResult result,
            HttpSession session,
            RedirectAttributes redirectAttributes,
            @RequestParam("image") MultipartFile image,
            Model model) throws IOException {

        if (result.hasErrors()) {
            result.getAllErrors().forEach(error -> log.info("Error: {}", error.getDefaultMessage()));
            model.addAttribute("restaurant", restaurantDTO);
            return "restaurantRegistrationView";  // Return to the form on errors
        }

        String imageUrl = cloudinaryService.uploadImageAndGetUrl(image);


        Owner owner = ownerService.getAuthenticatedOwner(session);

        Restaurant restaurant = restaurantService.createRestaurant(restaurantDTO, imageUrl, owner);

        Restaurant savedRestaurant = restaurantService.addRestaurant(restaurant, restaurant.getAddress(), owner);

        log.info("Restaurant registered: {}", savedRestaurant.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Restauracja " + savedRestaurant.getName() + " została pomyślnie zarejestrowana.");
        session.setAttribute("restaurant", savedRestaurant);
        return "redirect:/showAddingDeliveryStreetsView";  // Redirect to the next step
    }


    @GetMapping("/showAddingDeliveryStreetsView")
    public String showAddingDeliveryStreetsView(HttpSession session, Model model) {

        Restaurant restaurant = restaurantService.getOrCreateRestaurant(session);

        if (restaurant == null) {
            log.warn("No restaurant found in session.");
            return "redirect:/restaurantRegistrationForm"; // Redirect or display an error message
        }

        List<RestaurantDeliveryArea> deliveryAreas = restaurantService.findDeliveryAreaForRestaurant(restaurant.getRestaurantId());
        model.addAttribute("restaurantDeliveryAreas", deliveryAreas);

        log.debug("Loaded {} delivery areas for restaurant ID: {}", deliveryAreas.size(), restaurant.getRestaurantId());
        return "addingDeliveryStreetView";
    }


    @PostMapping("/addDeliveryStreet")
    public String addDeliveryStreet(@ModelAttribute("deliveryStreet") DeliveryStreetDTO deliveryStreetDTO,
                                    HttpSession session, RedirectAttributes redirectAttributes) {
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        if (restaurant == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono restauracji.");
            return "redirect:/showAddingDeliveryStreetsView";
        }

        Long restaurantId = restaurant.getRestaurantId();
        RestaurantDeliveryStreet newDeliveryStreet = RestaurantDeliveryStreet.builder()
                .streetName(deliveryStreetDTO.getStreetName())
                .postalCode(deliveryStreetDTO.getPostalCode())
                .district(deliveryStreetDTO.getDistrict())
                .build();

        restaurantService.addDeliveryStreetToRestaurant(restaurantId, newDeliveryStreet);
        redirectAttributes.addFlashAttribute("successMessage", "Nowa ulica dostawy została dodana.");

        return "redirect:/showAddingDeliveryStreetsView";
    }


    @GetMapping("/showAddMenuToTheRestaurantView")
    public String showAddMenuToTheRestaurantView(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            return "redirect:/login";
        }

        Owner owner = ownerService.getByUsername(username);
        if (owner == null) {
            model.addAttribute("error", "Owner not found.");
            return "errorPage";
        }

        Restaurant restaurant = ownerService.getRestaurantByOwnerId(owner.getOwnerId());
        if (restaurant == null) {
            return "redirect:/showRestaurantRegistrationForm";
        }

        List<RestaurantDeliveryArea> deliveryAreas = restaurantService.findDeliveryAreaForRestaurant(restaurant.getRestaurantId());
        if (deliveryAreas.isEmpty()) {
            return "redirect:/showAddingDeliveryStreetsView";
        }

        model.addAttribute("restaurant", restaurant);
        return "addMenuToTheRestaurantView";
    }

    @PostMapping("/addMenuToTheRestaurant")
    public String addMenuToTheRestaurant(@Valid @ModelAttribute("menuDTO") MenuDTO menuDTO,
                                         BindingResult result,
                                         HttpSession session,
                                         RedirectAttributes redirectAttributes) {
        log.info("Attempting to add menu to the restaurant");

        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("formErrors", result.getAllErrors());
            return "redirect:/addMenuToTheRestaurantView";
        }
        Restaurant restaurant = restaurantService.getCurrentRestaurant(session);

        if (restaurant == null) {
            log.info("restaurant for addMenuToTheRestaurant was null");
            redirectAttributes.addFlashAttribute("errorMessage", "No associated restaurant found.");
            return "redirect:/showRestaurantRegistrationForm";
        }

        Menu menu = menuDTOMapper.mapFromDTO(menuDTO, restaurant);
        Menu savedMenu = restaurantService.addMenu(menu);
        log.info("Menu added: {}", savedMenu);

        session.setAttribute("menuToUpdate", savedMenu);
        return "redirect:/addItemsToTheMenuView";
    }


    @GetMapping("/addItemsToTheMenuView")
    public String addMenuItemsToTheMenuView(HttpSession session, Model model) {
        log.debug("Starting to add menu items to the menu view.");

        Restaurant restaurant = restaurantService.getOrCreateRestaurant(session);
        Menu menu = restaurantService.getOrCreateMenu(session, restaurant);

        utilService.updateSessionAttributes(session, menu, restaurant);
        utilService.addModelAttributes(model, session);

        return "addingMenuItemsToTheMenu";
    }


    // TODO: Create test for this method. Problem with CloudinaryService
    @PostMapping("/addMenuItemToMenu")
    public String addMenuItemToMenu(@ModelAttribute("menuItem") MenuItemDTO menuItemDTO,
                                    BindingResult result,
                                    HttpSession session,
                                    RedirectAttributes redirectAttributes) throws IOException {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", result.getAllErrors());
            return "redirect:/addItemsToTheMenuView";
        }

        Menu menu = (Menu) session.getAttribute("menuToUpdate");
        Set<MenuItem> menuItems = utilService.getMenuItemsFromSession(session);
        String imageUrl = cloudinaryService.uploadImageAndGetUrl(menuItemDTO.getImage());
        MenuItem menuItem = menuItemDTOMapper.mapDTOtoMenuItem(menuItemDTO, imageUrl, menu);

        log.debug("Adding menu item to session: {}", menuItem);
        menuItems.add(menuItem);
        session.setAttribute("menuItems", menuItems);

        redirectAttributes.addFlashAttribute("successMessage", "Menu item added successfully");
        return "redirect:/addItemsToTheMenuView";
    }


    @GetMapping("/addAllMenuItemsToMenu")
    public String addAllMenuItemsToMenu(HttpSession session) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        Menu menu = (Menu) session.getAttribute("menuToUpdate");

        if (menu.getMenuId() == null) {
            User user = (User) session.getAttribute("user");
            Restaurant restaurantByUsername = restaurantService.getRestaurantByUsername(user.getUsername());
            menu = restaurantService.getMenuByRestaurant(restaurantByUsername);
        }
        log.info("########## RestaurantController ##### addAllMenuItemsToMenu # session.setAttribute(\"menu\",menu): {}", menu);

        session.setAttribute("menu", menu);
        for (MenuItem menuItem : menuItems) {
            restaurantService.addMenuItemToTheMenu(menuItem, menu);
        }
        session.removeAttribute("menuToUpdate");
        session.removeAttribute("groupedMenuItems");
        session.removeAttribute("menuItems");
        return "redirect:/showOwnerLoggedInView";
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

        List<FoodOrder> fooOrdersInProgressWithRestaurant = foodOrderService.getFoodOrders(foodOrdersInProgress, restaurant);

        model.addAttribute("foodOrdersInProgress", fooOrdersInProgressWithRestaurant);
        log.info("########## OwnerController #### showOrdersInProgress #  FINISH WITH foodOrdersInProgress {}",
                fooOrdersInProgressWithRestaurant);
        return "foodOrdersForRestaurantInProgressView";
    }


    @GetMapping("/changeMenu")
    public String changeMenu(HttpSession session) {
        Menu menu = (Menu) session.getAttribute("menu");
        log.info("########## RestaurantController ##### changeMenu # menu: {}", menu);
        if (menu == null) {
            User user = (User) session.getAttribute("user");
            Restaurant restaurantByUsername = restaurantService.getRestaurantByUsername(user.getUsername());

            menu = restaurantService.getMenuByRestaurant(restaurantByUsername);
        }

        Set<MenuItem> menuItemsByMenuId = restaurantService.getMenuItemsByMenuId(menu);
        menu = menu.withMenuItems(menuItemsByMenuId);
        restaurantService.deleteMenu(menu);

        return "redirect:/showAddMenuToTheRestaurantView";
    }



}
