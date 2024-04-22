package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import pl.dlusk.business.*;
import pl.dlusk.domain.*;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@AllArgsConstructor
public class RestaurantController {
    private final RestaurantService restaurantService;
    private final CloudinaryService cloudinaryService;
    private final OwnerService ownerService;
    private final PaymentService paymentService;
    private final ShoppingCartService shoppingCartService;

    @GetMapping("/restaurantMenu/{restaurantId}")
    public String showRestaurantMenu(@PathVariable Long restaurantId, Model model, HttpSession session) {
        log.info("Displaying menu for restaurant ID: {}", restaurantId);
        String username = (String) session.getAttribute("username");

        ShoppingCart shoppingCart = shoppingCartService.ensureShoppingCart(session, restaurantId, username);

        try {
            Menu menu = restaurantService.getMenuForRestaurantWithMenuItems(restaurantId);
            updateModelWithMenuDetails(model, shoppingCart, restaurantId, menu, session);
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
        restaurantDTO.setAddress(new RestaurantRegistrationDTO.AddressDTO()); // Ustawienie pustych domyślnych wartości
        model.addAttribute("restaurant", restaurantDTO);
        return "restaurantRegistrationView";
    }

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

        String imageUrl = uploadImageAndRetrieveUrl(image);


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
            return "redirect:/login";  // Ensure the user is logged in
        }

        Owner owner = ownerService.getByUsername(username);
        if (owner == null) {
            model.addAttribute("error", "Owner not found.");
            return "errorPage";  // Proper error handling
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
            redirectAttributes.addFlashAttribute("errorMessage", "No associated restaurant found.");
            return "redirect:/showRestaurantRegistrationForm";
        }

        Menu menu = convertToMenu(menuDTO, restaurant);
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

        updateSessionAttributes(session, menu, restaurant);
        addModelAttributes(model, session);

        return "addingMenuItemsToTheMenu";
    }

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
        Set<MenuItem> menuItems = getMenuItemsFromSession(session);
        String imageUrl = cloudinaryService.uploadImageAndGetUrl(menuItemDTO.getImage());
        MenuItem menuItem = convertDTOToMenuItem(menuItemDTO, imageUrl, menu);

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
            FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
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

    @GetMapping("/changeMenu")
    public String changeMenu(HttpSession session) {
        Menu menu = (Menu) session.getAttribute("menu");
        log.info("########## RestaurantController ##### changeMenu # menu: {}", menu);
        if (menu == null) {
            FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
            Restaurant restaurantByUsername = restaurantService.getRestaurantByUsername(user.getUsername());

            menu = restaurantService.getMenuByRestaurant(restaurantByUsername);
        }

        Set<MenuItem> menuItemsByMenuId = restaurantService.getMenuItemsByMenuId(menu);
        menu = menu.withMenuItems(menuItemsByMenuId);
        restaurantService.deleteMenu(menu);

        return "redirect:/showAddMenuToTheRestaurantView";
    }

    private Menu getMenuForRestaurantWithMenuItems(Long restaurantId) throws Exception {

        Menu menu = restaurantService.getMenuRestaurantById(restaurantId);
        if (menu == null) {
            throw new Exception("Menu not found for restaurantId: " + restaurantId);
        }
        Set<MenuItem> menuItems = restaurantService.getMenuItemsByMenuId(menu);
        return menu.withMenuItems(menuItems == null ? new HashSet<>() : menuItems);
    }

    private void updateModelWithMenuDetails(Model model, ShoppingCart shoppingCart, Long restaurantId, Menu menu, HttpSession session) {
        BigDecimal totalValue = paymentService.calculateTotalValue(shoppingCart);
        model.addAttribute("menu", menu);
        model.addAttribute("shoppingCart", shoppingCart);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("totalValue", totalValue);
        session.setAttribute("totalValue", totalValue);
        session.setAttribute("restaurantId", restaurantId);
    }

    private String uploadImageAndRetrieveUrl(MultipartFile image) throws IOException {
        Map uploadResult = cloudinaryService.uploadImage(image);
        return (String) uploadResult.get("url");
    }

    private Menu convertToMenu(MenuDTO menuDTO, Restaurant restaurant) {
        return Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .restaurant(restaurant)
                .build();
    }

    private void updateSessionAttributes(HttpSession session, Menu menu, Restaurant restaurant) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null) {
            menuItems = new HashSet<>();
        }
        session.setAttribute("menuItems", menuItems);
        session.setAttribute("menuToUpdate", menu);
        session.setAttribute("restaurant", restaurant);
    }

    private void addModelAttributes(Model model, HttpSession session) {
        Map<String, List<MenuItem>> groupedMenuItems = groupMenuItemsByCategory(session);
        model.addAttribute("menu", session.getAttribute("menuToUpdate"));
        model.addAttribute("groupedMenuItems", groupedMenuItems);
    }

    private Map<String, List<MenuItem>> groupMenuItemsByCategory(HttpSession session) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null || menuItems.isEmpty()) {
            return new HashMap<>();
        }
        return menuItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));
    }

    private Set<MenuItem> getMenuItemsFromSession(HttpSession session) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null) {
            return new HashSet<>();
        }
        return menuItems;
    }

    private MenuItem convertDTOToMenuItem(MenuItemDTO dto, String imageUrl, Menu menu) {
        return MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imagePath(imageUrl)
                .menu(menu)
                .build();
    }
}
