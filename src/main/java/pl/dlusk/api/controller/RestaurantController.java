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
import pl.dlusk.api.dto.RestaurantRegistrationDTO;
import pl.dlusk.business.CloudinaryService;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@AllArgsConstructor
public class RestaurantController {
    private final RestaurantDAO restaurantDAO;
    private FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    private final OwnerDAO ownerDAO;
    private final RestaurantService restaurantService;
    private final CloudinaryService cloudinaryService;

    @GetMapping("/restaurantMenu/{restaurantId}")
    public String showRestaurantMenu(@PathVariable Long restaurantId, Model model, HttpSession session) {
        log.info("Displaying menu for restaurant ID: {}", restaurantId);
        String username = (String) session.getAttribute("username");
        ShoppingCart shoppingCart = ensureShoppingCart(session, restaurantId, username);

        try {
            Menu menu = getMenuForRestaurant(restaurantId);
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
        ShoppingCart shoppingCart = getOrCreateShoppingCart(session);
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
        MenuItem menuItem = restaurantDAO.findMenuItemById(menuItemId);

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
            logErrors(result);
            model.addAttribute("restaurant", restaurantDTO);
            return "restaurantRegistrationView";  // Return to the form on errors
        }

        String imageUrl = uploadImageAndRetrieveUrl(image);
        Owner owner = getAuthenticatedOwner(session);
        Restaurant restaurant = createRestaurant(restaurantDTO, imageUrl, owner);

        Restaurant savedRestaurant = restaurantDAO.addRestaurant(restaurant, restaurant.getAddress(), owner);
        log.info("Restaurant registered: {}", savedRestaurant.getName());

        redirectAttributes.addFlashAttribute("successMessage", "Restauracja " + savedRestaurant.getName() + " została pomyślnie zarejestrowana.");
        session.setAttribute("restaurant", savedRestaurant);
        return "redirect:/showAddingDeliveryStreetsView";  // Redirect to the next step
    }

    private void logErrors(BindingResult result) {
        result.getAllErrors().forEach(error -> log.info("Error: {}", error.getDefaultMessage()));
    }

    private String uploadImageAndRetrieveUrl(MultipartFile image) throws IOException {
        Map uploadResult = cloudinaryService.uploadImage(image);
        return (String) uploadResult.get("url");
    }

    private Owner getAuthenticatedOwner(HttpSession session) {
        FoodOrderingAppUser appUser = (FoodOrderingAppUser) session.getAttribute("user");
        Long userId = foodOrderingAppUserRepository.findIdByUsername(appUser.getUsername());
        return ownerDAO.findByUserId(userId);
    }

    private Restaurant createRestaurant(RestaurantRegistrationDTO restaurantDTO, String imageUrl, Owner owner) {
        RestaurantAddress restaurantAddress = RestaurantAddress.builder()
                .city(restaurantDTO.getAddress().getCity())
                .postalCode(restaurantDTO.getAddress().getPostalCode())
                .address(restaurantDTO.getAddress().getAddress())
                .build();
        return Restaurant.builder()
                .name(restaurantDTO.getName())
                .description(restaurantDTO.getDescription())
                .imagePath(imageUrl)
                .address(restaurantAddress)
                .owner(owner)
                .build();
    }




    @GetMapping("/showAddingDeliveryStreetsView")
    public String showAddingDeliveryStreetsView(HttpSession session, Model model) {
        Restaurant restaurant = getRestaurantFromSession(session);
        if (restaurant == null) {
            log.warn("No restaurant found in session.");
            return "redirect:/restaurantRegistrationForm"; // Redirect or display an error message
        }

        List<RestaurantDeliveryArea> deliveryAreas = restaurantService.findDeliveryAreaForRestaurant(restaurant.getRestaurantId());
        model.addAttribute("restaurantDeliveryAreas", deliveryAreas);

        log.debug("Loaded {} delivery areas for restaurant ID: {}", deliveryAreas.size(), restaurant.getRestaurantId());
        return "addingDeliveryStreetView";
    }

    private Restaurant getRestaurantFromSession(HttpSession session) {
        return (Restaurant) session.getAttribute("restaurant");
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

        Owner owner = ownerDAO.findByUsername(username);
        if (owner == null) {
            model.addAttribute("error", "Owner not found.");
            return "errorPage";  // Proper error handling
        }

        Restaurant restaurant = restaurantDAO.getRestaurantByOwnerId(owner.getOwnerId());
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
            return "redirect:/addMenuToTheRestaurantView";  // assuming there's a view to add menu
        }

        Restaurant restaurant = getCurrentRestaurant(session);
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

    private Restaurant getCurrentRestaurant(HttpSession session) {
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        if (restaurant == null) {
            String username = (String) session.getAttribute("username");
            restaurant = restaurantService.getRestaurantByUsername(username);
            session.setAttribute("restaurant", restaurant);
        }
        return restaurant;
    }

    private Menu convertToMenu(MenuDTO menuDTO, Restaurant restaurant) {
        return Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .restaurant(restaurant)
                .build();
    }


    @GetMapping("/addItemsToTheMenuView")
    public String addMenuItemsToTheMenuView(HttpSession session, Model model) {
        log.info("########## RestaurantController ##### addMenuItemsToTheMenu # START");

        Menu menu = (Menu) session.getAttribute("menuToUpdate");
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
        String username = user.getUsername();

        if (restaurant == null) {
            log.info("########## RestaurantController ##### addMenuItemsToTheMenu # restaurant == null ");
            restaurant = restaurantDAO.findRestaurantByUsername(username);
            session.setAttribute("restaurant", restaurant);
        }

        log.info("########## RestaurantController ##### addMenuItemsToTheMenu # restaurant: {}", restaurant);

        if (menu == null) {
            log.info("########## RestaurantController ##### addMenuItemsToTheMenu # menu == null");
            menu = restaurantDAO.findMenuRestaurantById(restaurant.getRestaurantId());
            session.setAttribute("menu", menu);
        }

        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        Map<String, List<MenuItem>> groupedMenuItems = null;

        if (menuItems != null && !menuItems.isEmpty()) {
            groupedMenuItems = menuItems.stream()
                    .collect(Collectors.groupingBy(MenuItem::getCategory));
        }

        session.setAttribute("menuItems", menuItems == null ? new HashSet<MenuItem>() : menuItems);
        session.setAttribute("menuToUpdate", menu);
        session.setAttribute("restaurant", restaurant);

        log.info("########## RestaurantController ##### addMenuItemsToTheMenu # groupedMenuItems: {}", groupedMenuItems);


        model.addAttribute("menu", menu);
        model.addAttribute("groupedMenuItems", groupedMenuItems);
        log.info("########## RestaurantController ##### addMenuItemsToTheMenu # menuItems: {}", menuItems);

        return "addingMenuItemsToTheMenu";
    }

    @PostMapping("/addMenuItemToMenu")
    public String addMenuItemToSession(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("price") BigDecimal price,
            @RequestParam("category") String category,
            @RequestParam("image") MultipartFile image,
            HttpSession session) throws IOException {

        Menu menu = (Menu) session.getAttribute("menuToUpdate");
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null) {
            menuItems = new HashSet<>();
        }

        Map uploadResult = cloudinaryService.uploadImage(image);
        String imageUrl = (String) uploadResult.get("url");

        MenuItem menuItem = MenuItem.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .menu(menu)
                .imagePath(imageUrl)
                .build();

        log.info("########## RestaurantController ##### addMenuItemToSession # menuItem TO ADD: {}", menuItem);

        menuItems.add(menuItem);
        session.setAttribute("menuItems", menuItems);
        log.info("########## RestaurantController ##### addMenuItemToSession # menuItems: {}", menuItems);

        return "redirect:/addItemsToTheMenuView";
    }

    @GetMapping("/addAllMenuItemsToMenu")
    public String addAllMenuItemsToMenu(HttpSession session) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        Menu menu = (Menu) session.getAttribute("menuToUpdate");

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            log.info("Session attribute - Name: {}, Value: {}", attributeName, attributeValue);
        }
        if (menu.getMenuId() == null) {
            FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
            Restaurant restaurantByUsername = restaurantDAO.findRestaurantByUsername(user.getUsername());
            menu = restaurantDAO.findMenuRestaurantById(restaurantByUsername.getRestaurantId());
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
            Restaurant restaurantByUsername = restaurantDAO.findRestaurantByUsername(user.getUsername());
            menu = restaurantDAO.findMenuRestaurantById(restaurantByUsername.getRestaurantId());
        }
        Set<MenuItem> menuItemsByMenuId = restaurantDAO.findMenuItemsByMenuId(menu.getMenuId());
        menu = menu.withMenuItems(menuItemsByMenuId);

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            log.info("Session attribute - Name: {}, Value: {}", attributeName, attributeValue);
        }

        restaurantDAO.deleteMenu(menu);

        return "redirect:/showAddMenuToTheRestaurantView";
    }

    private ShoppingCart ensureShoppingCart(HttpSession session, Long restaurantId, String username) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
        if (shoppingCart == null || !shoppingCart.getRestaurantId().equals(restaurantId)) {
            Long userId = foodOrderingAppUserRepository.findIdByUsername(username);
            shoppingCart = ShoppingCart.builder()
                    .userId(userId)
                    .restaurantId(restaurantId)
                    .build();
            session.setAttribute("shoppingCart", shoppingCart);
        }
        return shoppingCart;
    }

    private Menu getMenuForRestaurant(Long restaurantId) throws Exception {
        Menu menu = restaurantService.getMenuRestaurantById(restaurantId);
        if (menu == null) {
            throw new Exception("Menu not found for restaurantId: " + restaurantId);
        }
        Set<MenuItem> menuItems = restaurantService.getMenuItemsByMenuId(menu.getMenuId());
        return menu.withMenuItems(menuItems == null ? new HashSet<>() : menuItems);
    }

    private void updateModelWithMenuDetails(Model model, ShoppingCart shoppingCart, Long restaurantId, Menu menu, HttpSession session) {
        BigDecimal totalValue = calculateTotalValue(shoppingCart);
        model.addAttribute("menu", menu);
        model.addAttribute("shoppingCart", shoppingCart);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("totalValue", totalValue);
        session.setAttribute("totalValue", totalValue);
        session.setAttribute("restaurantId", restaurantId);
    }

    private BigDecimal calculateTotalValue(ShoppingCart shoppingCart) {
        return shoppingCart.getItems().entrySet().stream()
                .map(entry -> entry.getKey().getPrice().multiply(BigDecimal.valueOf(entry.getValue())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private ShoppingCart getOrCreateShoppingCart(HttpSession session) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = ShoppingCart.builder().build();
            session.setAttribute("shoppingCart", shoppingCart);
        }
        return shoppingCart;
    }
}
