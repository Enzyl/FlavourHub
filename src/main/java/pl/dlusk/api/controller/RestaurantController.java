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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.database.repository.jpa.MenuJpaRepository;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Controller
@AllArgsConstructor
public class RestaurantController {
    private final MenuJpaRepository menuJpaRepository;
    private final RestaurantDAO restaurantDAO;
    private FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    private final OwnerDAO ownerDAO;
private final RestaurantService restaurantService;
    @GetMapping("/restaurantMenu/{restaurantId}")
    public String showRestaurantMenu(@PathVariable Long restaurantId, Model model, HttpSession session) {
        log.info("########## RestaurantController ##### showRestaurantMenu #### restaurantId: " + restaurantId);

        String username = (String) session.getAttribute("username");

        try {
            ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");


            if (shoppingCart == null || !shoppingCart.getRestaurantId().equals(restaurantId)) {
                Long idByUsername = foodOrderingAppUserRepository.findIdByUsername(username);

                shoppingCart = ShoppingCart.builder()
                        .userId(idByUsername)
                        .restaurantId(restaurantId)
                        .build();
                ShoppingCart shoppingCartWithRestaurantId = shoppingCart.withRestaurantId(restaurantId);
                session.setAttribute("shoppingCart", shoppingCartWithRestaurantId);
            }

            Menu menu = restaurantDAO.findMenuRestaurantById(restaurantId);
            log.info("########## RestaurantController ##### showRestaurantMenu # menu : " + menu.toString());
            Set<MenuItem> menuItemsByMenuId = restaurantDAO.findMenuItemsByMenuId(menu.getMenuId());
            menu = menu.withMenuItems(menuItemsByMenuId);
            if (menu != null && menu.getMenuItems() == null) {
                menu = menu.withMenuItems(new HashSet<>());
            }

            BigDecimal totalValue = shoppingCart.getItems().entrySet().stream()
                    .map(entry -> entry.getKey().getPrice().multiply(new BigDecimal(entry.getValue())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            model.addAttribute("totalValue", totalValue);
            session.setAttribute("totalValue", totalValue);


            model.addAttribute("menu", menu);
            model.addAttribute("restaurantId", restaurantId);
            model.addAttribute("shoppingCart", shoppingCart);

            session.setAttribute("restaurantId", restaurantId);
            return "restaurantMenu";
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Menu dla restauracji o ID " + restaurantId + " nie zostało znalezione.");
            return "errorPage";
        }
    }

    @PostMapping("/addToCart")
    public String addToCart(@RequestParam("menuItemId") Long menuItemId, HttpSession session, RedirectAttributes redirectAttributes) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");

        MenuItem menuItemById = restaurantDAO.findMenuItemById(menuItemId);
        shoppingCart.addItem(menuItemById);
        session.setAttribute("shoppingCart", shoppingCart);

        Long restaurantId = shoppingCart.getRestaurantId();
        redirectAttributes.addFlashAttribute("shoppingCart", shoppingCart);

        return "redirect:/restaurantMenu/" + restaurantId;
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
        log.info("########## RestaurantController ##### showRegisterRestaurantForm # START");
        Restaurant restaurant = Restaurant.builder()
                .address(RestaurantAddress.builder()
                        .address("")
                        .city("")
                        .postalCode("")
                        .build())
                .owner(null)
                .name("")
                .imagePath("")
                .description("")
                .build();
        model.addAttribute("restaurant", restaurant);

        return "restaurantRegistrationView";
    }

    @PostMapping("/registerRestaurant")
    public String registerRestaurant(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            @RequestParam("address.city") String city,
            @RequestParam("address.postalCode") String postalCode,
            @RequestParam("address.address") String address,
            HttpSession session,
            RedirectAttributes redirectAttributes) {

        log.info("########## RestaurantController ##### registerRestaurant # START");

        RestaurantAddress restaurantAddress = RestaurantAddress.builder()
                .city(city)
                .postalCode(postalCode)
                .address(address)
                .build();

        log.info("Registering restaurant with name: {}", name);
        FoodOrderingAppUser appUser = (FoodOrderingAppUser) session.getAttribute("user");
        log.info("Registering restaurant with appUser: {}", appUser);
        Long userId = foodOrderingAppUserRepository.findIdByUsername(appUser.getUsername());
        Owner owner = ownerDAO.findByUserId(userId);
        log.info("########## RestaurantController ##### registerRestaurant # userId : " + userId);

        log.info("########## RestaurantController ##### registerRestaurant # owner : " + owner);

        Restaurant restaurant = Restaurant.builder()
                .name(name)
                .description(description)
                .address(restaurantAddress)
                .owner(owner)
                .build();

        log.info("########## RestaurantController ##### registerRestaurant # restaurant : " + restaurant.toString());


        Restaurant savedRestaurant = restaurantDAO.addRestaurant(restaurant, restaurantAddress, owner);
        log.info("Registered restaurant : {}", savedRestaurant);



        redirectAttributes.addFlashAttribute("successMessage", "Restauracja " + name + " została pomyślnie zarejestrowana.");
        session.setAttribute("restaurant", savedRestaurant);
        return "redirect:/showAddingDeliveryStreetsView";
    }

    @GetMapping("/showAddingDeliveryStreetsView")
    public String   showAddingDeliveryStreetsView(HttpSession session, Model model) {

//        Enumeration<String> attributeNames = session.getAttributeNames();
//        while (attributeNames.hasMoreElements()) {
//            String attributeName = attributeNames.nextElement();
//            Object attributeValue = session.getAttribute(attributeName);
//            log.info("Session attribute - Name: {}, Value: {}", attributeName, attributeValue);
//        }

        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        Long restaurantId = restaurant.getRestaurantId();
        List<RestaurantDeliveryArea> restaurantDeliveryAreas =  restaurantService
                .findDeliveryAreaForRestaurant(restaurantId);
        log.info("########## RestaurantController ##### showAddingDeliveryStreetsView # restaurantDeliveryAreas : " + restaurantDeliveryAreas);


        RestaurantDeliveryStreet testStreet = RestaurantDeliveryStreet.builder()
                .streetName("testStreet")
                .district("testDistrict")
                .postalCode("01-test")
                .build();

        RestaurantDeliveryArea testArea = RestaurantDeliveryArea.builder()
                .deliveryStreet(testStreet)
                .build();
        List<RestaurantDeliveryArea> testAreas = new ArrayList<>();
        testAreas.add(testArea);

        model.addAttribute("restaurantDeliveryAreas",restaurantDeliveryAreas);
        session.setAttribute("restaurantDeliveryAreas",restaurantDeliveryAreas);
        return "addingDeliveryStreetView";
    }
    @PostMapping("/addDeliveryStreet")
    public String addDeliveryStreet(@RequestParam("streetName") String streetName,
                                    @RequestParam("postalCode") String postalCode,
                                    @RequestParam("district") String district,
                                    HttpSession session, RedirectAttributes redirectAttributes, Model model) {
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        if (restaurant != null) {
            Long restaurantId = restaurant.getRestaurantId();
            RestaurantDeliveryStreet newDeliveryStreet = RestaurantDeliveryStreet.builder()
                    .streetName(streetName)
                    .postalCode(postalCode)
                    .district(district)
                    .build();

            restaurantService.addDeliveryStreetToRestaurant(restaurantId, newDeliveryStreet);

            redirectAttributes.addFlashAttribute("successMessage", "Nowa ulica dostawy została dodana.");
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Nie znaleziono restauracji.");
        }

        return "redirect:/showAddingDeliveryStreetsView";
    }


    @GetMapping("/showAddMenuToTheRestaurantView")
    public String   addMenuToTheRestaurantView(HttpSession session) {



        Owner owner = ownerDAO.findByUsername((String) session.getAttribute("username"));



        Restaurant restaurantByOwnerId = restaurantDAO.getRestaurantByOwnerId(owner.getOwnerId());


        List<RestaurantDeliveryArea> deliveryAreaForRestaurant = restaurantService
                .findDeliveryAreaForRestaurant(restaurantByOwnerId.getRestaurantId());

        log.info("########## RestaurantController ##### addMenuToTheRestaurantView # deliveryAreaForRestaurant : " + deliveryAreaForRestaurant);


        boolean b = deliveryAreaForRestaurant.size() == 0;

        session.setAttribute("restaurant",restaurantByOwnerId);
        if (b){



            return "redirect:/showAddingDeliveryStreetsView";
        }
        return "addMenuToTheRestaurantView";
    }

    @PostMapping("/addMenuToTheRestaurant")
    public String addMenuToTheRestaurant(
            @RequestParam("name") String name,
            @RequestParam("description") String description,
            HttpSession session) {
        log.info("########## RestaurantController ##### addMenuToTheRestaurant # START");
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        String username = (String) session.getAttribute("username");
        if (restaurant == null) {
            restaurant = restaurantDAO.findRestaurantByUsername(username);
        }
        Menu menu = Menu.builder()
                .name(name)
                .description(description)
                .restaurant(restaurant)
                .build();
        Menu savedMenu = restaurantDAO.save(menu);
        log.info("########## RestaurantController ##### addMenuToTheRestaurant # saved menu: {}", menu);


        session.setAttribute("menuToUpdate", savedMenu);
        log.info("########## RestaurantController ##### addMenuToTheRestaurant # FINISHED");
        return "redirect:/addItemsToTheMenuView";
    }

    @GetMapping("/addItemsToTheMenuView")
    public String addMenuItemsToTheMenu(HttpSession session, Model model) {
        log.info("########## RestaurantController ##### addMenuItemsToTheMenu # START");

        Menu menu = (Menu) session.getAttribute("menuToUpdate");
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
        String username = user.getUsername();

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            log.info("Session attribute - Name: {}, Value: {}", attributeName, attributeValue);
        }

        if (restaurant == null) {
            log.info("########## RestaurantController ##### addMenuItemsToTheMenu # restaurant == null ");
            restaurant = restaurantDAO.findRestaurantByUsername(username);
            session.setAttribute("restaurant",restaurant);
        }

        log.info("########## RestaurantController ##### addMenuItemsToTheMenu # restaurant: {}", restaurant);

        if (menu == null) {
            log.info("########## RestaurantController ##### addMenuItemsToTheMenu # menu == null");
            menu = restaurantDAO.findMenuRestaurantById(restaurant.getRestaurantId());
            session.setAttribute("menu",menu);
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
            HttpSession session) {

        Menu menu = (Menu) session.getAttribute("menuToUpdate");
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null) {
            menuItems = new HashSet<>();
        }
        MenuItem menuItem = MenuItem.builder()
                .name(name)
                .description(description)
                .price(price)
                .category(category)
                .menu(menu)
                .build();
        log.info("########## RestaurantController ##### addMenuItemToSession # menuItem TO ADD: {}", menuItem);

        menuItems.add(menuItem);
        session.setAttribute("menuItems", menuItems);
        log.info("########## RestaurantController ##### addMenuItemToSession # menuItems: {}", menuItems);

        return "redirect:/addItemsToTheMenuView";
    }
    @GetMapping("/addAllMenuItemsToMenu")
    public String addAllMenuItemsToMenu(HttpSession session, Model model){
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        Menu menu = (Menu) session.getAttribute("menuToUpdate");

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            Object attributeValue = session.getAttribute(attributeName);
            log.info("Session attribute - Name: {}, Value: {}", attributeName, attributeValue);
        }
        if (menu.getMenuId() == null){
            FoodOrderingAppUser user = (FoodOrderingAppUser) session.getAttribute("user");
            Restaurant restaurantByUsername = restaurantDAO.findRestaurantByUsername(user.getUsername());
            menu = restaurantDAO.findMenuRestaurantById(restaurantByUsername.getRestaurantId());
        }
        log.info("########## RestaurantController ##### addAllMenuItemsToMenu # session.setAttribute(\"menu\",menu): {}", menu);

        session.setAttribute("menu",menu);
        for (MenuItem menuItem : menuItems) {
            restaurantService.addMenuItemToTheMenu(menuItem,menu);
        }
        session.removeAttribute("menuToUpdate");
        session.removeAttribute("groupedMenuItems");
        session.removeAttribute("menuItems");
        return "redirect:/showOwnerLoggedInView";
    }

    @GetMapping("/changeMenu")
    public String changeMenu(HttpSession session, Model model){
        Menu menu = (Menu) session.getAttribute("menu");
        log.info("########## RestaurantController ##### changeMenu # menu: {}", menu);
        if (menu == null){
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
}
