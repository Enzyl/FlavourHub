package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.api.dto.DeliveryStreetDTO;
import pl.dlusk.api.dto.MenuDTO;
import pl.dlusk.api.dto.RestaurantRegistrationDTO;
import pl.dlusk.api.dto.mapper.MenuDTOMapper;
import pl.dlusk.business.*;
import pl.dlusk.domain.*;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.User;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantControllerTest {
    private final Long restaurantId = 1L;
    private final String username = "testUser";
    @Mock
    private RestaurantService restaurantService;
    @Mock
    private ShoppingCartService shoppingCartService;
    @Mock
    private HttpSession session;
    @Mock
    private RedirectAttributes redirectAttributes;
    @Mock
    private PaymentService paymentService;
    @Mock
    private Model model;
    @Mock
    private BindingResult result;
    @Mock
    private OwnerService ownerService;
    @Mock
    private FoodOrderService foodOrderService;
    @InjectMocks
    private RestaurantController restaurantController;
    @Mock
    private ShoppingCart shoppingCart;
    @Mock
    private Menu menu;
    @Mock
    private UtilService utilService;
    private BigDecimal totalValue;
    @Mock
    private MenuItem menuItem;
    private Long menuItemId = 1L;
    @Mock
    private MenuDTOMapper menuDTOMapper;
    @Mock
    private Restaurant restaurant;

    @BeforeEach
    void setUp() throws Exception {
        menu = Menu.builder()
                .menuId(restaurantId)
                .name("Italian")
                .build();


        menuItem = MenuItem.builder()
                .menuItemId(1L)
                .name("Pizza")
                .price(BigDecimal.valueOf(10.00))
                .build();

        totalValue = BigDecimal.valueOf(100.00);

    }

    @Test
    public void showRestaurantMenu_WithExistingMenu_ReturnsMenuView() throws Exception {
        // Arrange
        when(session.getAttribute("username")).thenReturn(username);
        when(restaurantService.getMenuForRestaurantWithMenuItems(restaurantId)).thenReturn(menu);
        when(shoppingCartService.ensureShoppingCart(session, restaurantId, username)).thenReturn(shoppingCart);

        doAnswer(invocation -> {
            Model model = invocation.getArgument(0);
            Menu menu = invocation.getArgument(3);
            model.addAttribute("menu", menu);
            model.addAttribute("shoppingCart", invocation.getArgument(1));
            model.addAttribute("restaurantId", invocation.getArgument(2));
            BigDecimal totalValue = paymentService.calculateTotalValue(invocation.getArgument(1));
            model.addAttribute("totalValue", totalValue);
            session.setAttribute("totalValue", totalValue);
            session.setAttribute("restaurantId", invocation.getArgument(2));
            return null;
        }).when(utilService).updateModelWithMenuDetails(any(Model.class), any(ShoppingCart.class), anyLong(), any(Menu.class), any(HttpSession.class));

        // Act
        String viewName = restaurantController.showRestaurantMenu(restaurantId, model, session);

        // Assert
        assertEquals("restaurantMenu", viewName);
        verify(utilService).updateModelWithMenuDetails(eq(model), eq(shoppingCart), eq(restaurantId), eq(menu), eq(session));
        verify(model).addAttribute("menu", menu);
        verify(model).addAttribute("shoppingCart", shoppingCart);
        verify(model).addAttribute("restaurantId", restaurantId);
        verify(session).setAttribute(eq("totalValue"), any());
        verify(session).setAttribute("restaurantId", restaurantId);
    }


    @Test
    void showRestaurantMenu_ShouldReturnErrorPageWhenExceptionThrown() throws Exception {
        // Arrange
        when(restaurantService.getMenuForRestaurantWithMenuItems(restaurantId))
                .thenThrow(new RuntimeException("Database error"));

        // Act
        String viewName = restaurantController.showRestaurantMenu(restaurantId, model, session);

        // Assert
        assertEquals("errorPage", viewName);
        verify(model).addAttribute("errorMessage", "Menu for restaurant ID " + restaurantId + " could not be found.");
    }

    @Test
    void addToCart_ItemAddedSuccessfully() {
        // Arrange
        when(restaurantService.getMenuItemById(menuItemId)).thenReturn(menuItem);
        when(shoppingCartService.getOrCreateShoppingCart(any(HttpSession.class))).thenReturn(shoppingCart);
        when(shoppingCart.getRestaurantId()).thenReturn(restaurantId);

        // Act
        String result = restaurantController.addToCart(menuItemId, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/restaurantMenu/" + restaurantId, result);
        verify(shoppingCartService).getOrCreateShoppingCart(session);
        verify(restaurantService).getMenuItemById(menuItemId);
        verify(shoppingCart).addItem(menuItem);
        verify(session).setAttribute("shoppingCart", shoppingCart);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Item added to cart successfully.");
    }

    @Test
    void addToCart_FailedToAddItem() {
        // Arrange
        when(restaurantService.getMenuItemById(menuItemId)).thenThrow(new RuntimeException("Database error"));
        when(shoppingCartService.getOrCreateShoppingCart(any(HttpSession.class))).thenReturn(shoppingCart);
        when(shoppingCart.getRestaurantId()).thenReturn(restaurantId);

        // Act
        String result = restaurantController.addToCart(menuItemId, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/restaurantMenu/" + restaurantId, result);
        verify(shoppingCartService).getOrCreateShoppingCart(session);
        verify(restaurantService).getMenuItemById(menuItemId);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Failed to add item to cart.");


    }

    @Test
    void updateCartItem_UpdateQuantitySuccessfully() {
        // Arrange
        int quantity = 5;
        when(session.getAttribute("shoppingCart")).thenReturn(shoppingCart);
        when(shoppingCart.getRestaurantId()).thenReturn(restaurantId);
        when(restaurantService.getMenuItemById(menuItemId)).thenReturn(menuItem);

        // Act
        String returnedView = restaurantController.updateCartItem(menuItemId, quantity, session);

        // Assert
        assertEquals("redirect:/restaurantMenu/" + restaurantId, returnedView);
        verify(shoppingCart).updateItemQuantity(menuItem, quantity);
        verify(session).setAttribute("shoppingCart", shoppingCart);
    }


    @Test
    void updateCartItem_RemoveItemSuccessfully() {
        // Arrange
        int quantity = 0;
        when(session.getAttribute("shoppingCart")).thenReturn(shoppingCart);
        when(shoppingCart.getRestaurantId()).thenReturn(restaurantId);
        when(restaurantService.getMenuItemById(menuItemId)).thenReturn(menuItem);
        // Act
        String returnedView = restaurantController.updateCartItem(menuItemId, quantity, session);

        // Assert
        assertEquals("redirect:/restaurantMenu/" + restaurantId, returnedView);
        verify(shoppingCart).removeItem(menuItem);
        verify(session).setAttribute("shoppingCart", shoppingCart);
    }

    @Test
    void updateCartItem_NoShoppingCartFound() {
        // Arrange
        when(session.getAttribute("shoppingCart")).thenReturn(null);
        int quantity = 2;

        // Act
        String returnedView = restaurantController.updateCartItem(menuItemId, quantity, session);

        // Assert
        assertEquals("errorPage", returnedView);
        verify(session, never()).setAttribute(eq("shoppingCart"), any(ShoppingCart.class));
        verify(restaurantService, never()).getMenuItemById(anyLong());
        verifyNoMoreInteractions(shoppingCart);
    }

    @Test
    void showRegisterRestaurantForm_ShouldAddRestaurantDTOToModelAndReturnView() {
        // Arrange

        // Act
        String viewName = restaurantController.showRegisterRestaurantForm(model);

        // Assert
        assertEquals("restaurantRegistrationView", viewName, "Expected to return view name 'restaurantRegistrationView'.");

        // Verify that the model has an attribute "restaurant" which is a RestaurantRegistrationDTO
        verify(model, times(1)).addAttribute(eq("restaurant"), any(RestaurantRegistrationDTO.class));
    }

    @Test
    void registerRestaurant_WithFormErrors() throws IOException {
        // Arrange
        RestaurantRegistrationDTO restaurantDTO = new RestaurantRegistrationDTO();
        when(result.hasErrors()).thenReturn(true);
        when(result.getAllErrors()).thenReturn(Collections.emptyList());

        // Act
        String viewName = restaurantController.registerRestaurant(
                restaurantDTO, result, session, redirectAttributes,
                new MockMultipartFile("image", "test.png", "image/png", new byte[0]),
                model);

        // Assert
        assertEquals("restaurantRegistrationView", viewName);
        verify(model, times(1)).addAttribute("restaurant", restaurantDTO);
        verifyNoInteractions(restaurantService);
    }

    @Test
    public void showAddingDeliveryStreetsView_withRestaurantInSession_returnsViewAndDeliveryAreas() {
        // Arrange
        restaurant = Restaurant.builder()
                .restaurantId(restaurantId)
                .build();

        List<RestaurantDeliveryArea> deliveryAreas = new ArrayList<>();
        when(restaurantService.getOrCreateRestaurant(session)).thenReturn(restaurant);
        when(restaurantService.findDeliveryAreaForRestaurant(restaurant.getRestaurantId())).thenReturn(deliveryAreas);

        // Act
        String viewName = restaurantController.showAddingDeliveryStreetsView(session, model);

        // Assert
        assertEquals("addingDeliveryStreetView", viewName);
        verify(restaurantService).getOrCreateRestaurant(session);
        verify(restaurantService).findDeliveryAreaForRestaurant(restaurant.getRestaurantId());
        verify(model).addAttribute("restaurantDeliveryAreas", deliveryAreas);
        verifyNoMoreInteractions(restaurantService);
    }

    @Test
    public void showAddingDeliveryStreetsView_withNoRestaurantInSession_redirectsToRegistration() {
        // Arrange
        when(restaurantService.getOrCreateRestaurant(session)).thenReturn(null);

        // Act
        String viewName = restaurantController.showAddingDeliveryStreetsView(session, model);

        // Assert
        assertEquals("redirect:/restaurantRegistrationForm", viewName);
        verify(restaurantService).getOrCreateRestaurant(session);
        verifyNoMoreInteractions(restaurantService);
    }

    @Test
    public void addDeliveryStreet_withValidData_addsStreetAndRedirects() {
        // Arrange
        DeliveryStreetDTO deliveryStreetDTO = new DeliveryStreetDTO("Main St", "12-345", "Downtown");
        restaurant = Restaurant.builder()
                .restaurantId(restaurantId)
                .build();
        when(session.getAttribute("restaurant")).thenReturn(restaurant);
        RestaurantDeliveryStreet restaurantDeliveryStreet = RestaurantDeliveryStreet.builder()
                .streetName(deliveryStreetDTO.getStreetName())
                .postalCode(deliveryStreetDTO.getPostalCode())
                .district(deliveryStreetDTO.getDistrict())
                .build();

        // Act
        String viewName = restaurantController.addDeliveryStreet(deliveryStreetDTO, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/showAddingDeliveryStreetsView", viewName);
        verify(restaurantService).addDeliveryStreetToRestaurant(restaurant.getRestaurantId(), restaurantDeliveryStreet);

        verify(redirectAttributes).addFlashAttribute("successMessage", "Nowa ulica dostawy została dodana.");

    }

    @Test
    public void addDeliveryStreet_withNoRestaurantInSession_redirectsWithError() {
        // Arrange
        DeliveryStreetDTO deliveryStreetDTO = new DeliveryStreetDTO("Main St", "12-345", "Downtown");
        when(session.getAttribute("restaurant")).thenReturn(null);

        // Act
        String viewName = restaurantController.addDeliveryStreet(deliveryStreetDTO, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/showAddingDeliveryStreetsView", viewName);
        verifyNoInteractions(restaurantService);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Nie znaleziono restauracji.");
    }


    @Test
    public void showAddMenuToTheRestaurantView_withLoggedInUserAndRestaurant_returnsViewAndRestaurant() {
        // Arrange
        String username = "testUser";
        Owner owner = Owner.builder().build();
        Restaurant restaurant = Restaurant.builder().build();
        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getByUsername(username)).thenReturn(owner);
        when(ownerService.getRestaurantByOwnerId(owner.getOwnerId())).thenReturn(restaurant);
        when(restaurantService.findDeliveryAreaForRestaurant(restaurant.getRestaurantId())).thenReturn(
                Collections.singletonList(RestaurantDeliveryArea.builder().build()));

        // Act
        String viewName = restaurantController.showAddMenuToTheRestaurantView(session, model);

        // Assert
        assertEquals("addMenuToTheRestaurantView", viewName);
        verify(session).getAttribute("username");
        verify(ownerService).getByUsername(username);
        verify(ownerService).getRestaurantByOwnerId(owner.getOwnerId());
        verify(restaurantService).findDeliveryAreaForRestaurant(restaurant.getRestaurantId());
        verify(model).addAttribute("restaurant", restaurant);
    }

    @Test
    public void showAddMenuToTheRestaurantView_withNoUsernameInSession_redirectsToLogin() {
        // Arrange
        when(session.getAttribute("username")).thenReturn(null);

        // Act
        String viewName = restaurantController.showAddMenuToTheRestaurantView(session, model);

        // Assert
        assertEquals("redirect:/login", viewName);
        verify(session).getAttribute("username");
        verifyNoMoreInteractions(ownerService, restaurantService, model);
    }

    @Test
    public void showAddMenuToTheRestaurantView_withNoOwnerByUsername_returnsErrorPage() {
        // Arrange
        String username = "testUser";
        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getByUsername(username)).thenReturn(null);

        // Act
        String viewName = restaurantController.showAddMenuToTheRestaurantView(session, model);

        // Assert
        assertEquals("errorPage", viewName);
        verify(session).getAttribute("username");
        verify(ownerService).getByUsername(username);
        verify(model).addAttribute("error", "Owner not found.");
        verifyNoMoreInteractions(ownerService, restaurantService, model);
    }

    @Test
    public void showAddMenuToTheRestaurantView_withNoRestaurantByOwnerId_redirectsToRegistration() {
        // Arrange
        String username = "testUser";
        Owner owner = Owner.builder().build();
        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getByUsername(username)).thenReturn(owner);
        when(ownerService.getRestaurantByOwnerId(owner.getOwnerId())).thenReturn(null);

        // Act
        String viewName = restaurantController.showAddMenuToTheRestaurantView(session, model);

        // Assert
        assertEquals("redirect:/showRestaurantRegistrationForm", viewName);
        verify(session).getAttribute("username");
        verify(ownerService).getByUsername(username);
        verify(ownerService).getRestaurantByOwnerId(owner.getOwnerId());
        verifyNoMoreInteractions(restaurantService, model);
    }

    @Test
    public void showAddMenuToTheRestaurantView_withNoDeliveryAreas_redirectsToAddDeliveryStreets() {
        // Arrange
        String username = "testUser";
        Owner owner = Owner.builder().build();
        Restaurant restaurant = Restaurant.builder().build();
        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getByUsername(username)).thenReturn(owner);
        when(ownerService.getRestaurantByOwnerId(owner.getOwnerId())).thenReturn(restaurant);
        when(restaurantService.findDeliveryAreaForRestaurant(restaurant.getRestaurantId())).thenReturn(Collections.emptyList());

        // Act
        String viewName = restaurantController.showAddMenuToTheRestaurantView(session, model);

        // Assert
        assertEquals("redirect:/showAddingDeliveryStreetsView", viewName);
        verify(session).getAttribute("username");
        verify(ownerService).getByUsername(username);
        verify(ownerService).getRestaurantByOwnerId(owner.getOwnerId());
        verify(restaurantService).findDeliveryAreaForRestaurant(restaurant.getRestaurantId());
        verifyNoMoreInteractions(model);


    }

    @Test
    public void addMenuToTheRestaurant_withValidData_addsMenuAndRedirects() {
        // Arrange
        Menu expectedMenu = Menu.builder()
                .name("Italian") // Set a name for the menu
                .build();
        MenuDTO menuDTO = new MenuDTO("Italian", "", null);
        Restaurant restaurant = Restaurant.builder().build();
        when(restaurantService.getCurrentRestaurant(session)).thenReturn(restaurant);

        // Mock menu creation with any Menu object (flexible approach)
        ArgumentCaptor<Menu> menuCaptor = ArgumentCaptor.forClass(Menu.class);
        when(restaurantService.addMenu(menuCaptor.capture())).thenReturn(expectedMenu);


        when(result.hasErrors()).thenReturn(false);

        // Act
        String viewName = restaurantController.addMenuToTheRestaurant(menuDTO, result, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/addItemsToTheMenuView", viewName);

        verify(restaurantService).getCurrentRestaurant(session);
        verify(restaurantService).addMenu(menuDTOMapper.mapFromDTO(menuDTO, restaurant));
        verify(session).setAttribute("menuToUpdate", expectedMenu);
    }


    @Test
    public void addMenuToTheRestaurant_withInvalidData_redirectsWithError() {
        // Arrange
        MenuDTO menuDTO = new MenuDTO();
        when(result.hasErrors()).thenReturn(true);

        // Act
        String viewName = restaurantController.addMenuToTheRestaurant(menuDTO, result, session, redirectAttributes);

        // Assert
        assertEquals("redirect:/addMenuToTheRestaurantView", viewName);
        verify(result).hasErrors();
        verify(redirectAttributes).addFlashAttribute("formErrors", result.getAllErrors());
        verifyNoMoreInteractions(restaurantService, session);
    }

    @Test
    public void addMenuToTheRestaurant_withNoRestaurantInSession_redirectsWithError() {
        // Arrange
        MenuDTO menuDTO = new MenuDTO("Summer Menu", "", null);
        when(restaurantService.getCurrentRestaurant(session)).thenReturn(null);
        when(result.hasErrors()).thenReturn(false);

// Act
        String viewName = restaurantController.addMenuToTheRestaurant(menuDTO, result, session, redirectAttributes);

// Assert
        assertEquals("redirect:/showRestaurantRegistrationForm", viewName);
        verify(restaurantService).getCurrentRestaurant(session);
        verify(result).hasErrors();
        verify(redirectAttributes).addFlashAttribute("errorMessage", "No associated restaurant found.");
        verifyNoMoreInteractions(restaurantService);
    }

    @Test
    public void addMenuItemsToTheMenuView_ShouldReturnViewName() {
        // Arrange
        Restaurant restaurant = Restaurant.builder().build();
        Menu menu = Menu.builder().build();

        when(restaurantService.getOrCreateRestaurant(session)).thenReturn(restaurant);
        when(restaurantService.getOrCreateMenu(session, restaurant)).thenReturn(menu);

        // Act
        String viewName = restaurantController.addMenuItemsToTheMenuView(session, model);

        // Assert
        assertEquals("addingMenuItemsToTheMenu", viewName);
        verify(restaurantService).getOrCreateRestaurant(session);
        verify(restaurantService).getOrCreateMenu(session, restaurant);
        verify(utilService).updateSessionAttributes(session, menu, restaurant);
        verify(utilService).addModelAttributes(model, session);
    }

    @Test
    void addAllMenuItemsToMenu_WithExistingMenuItemsAndMenu() {
        // Arrange
        HashSet menuItems = new HashSet<>();
        when(session.getAttribute("menuItems")).thenReturn(menuItems);
        when(session.getAttribute("menuToUpdate")).thenReturn(menu);

        // Act
        String viewName = restaurantController.addAllMenuItemsToMenu(session);

        // Assert
        assertEquals("redirect:/showOwnerLoggedInView", viewName);
        verify(session).setAttribute("menu", menu);
        verify(restaurantService, times(menuItems.size())).addMenuItemToTheMenu(any(MenuItem.class), eq(menu));
        verify(session).removeAttribute("menuToUpdate");
        verify(session).removeAttribute("groupedMenuItems");
        verify(session).removeAttribute("menuItems");
    }

    @Test
    public void addAllMenuItemsToMenu_withNullMenuId_findsMenuByRestaurantAndSavesItems() {
        // Arrange
        Set<MenuItem> existingMenuItems = new HashSet<>();
        existingMenuItems.add(MenuItem.builder().name("Pizza").build());
        User user = Mockito.mock(User.class);
        Restaurant restaurant = Restaurant.builder().build();
        Menu expectedMenu = Menu.builder().restaurant(restaurant).build();

        when(session.getAttribute("menuItems")).thenReturn(existingMenuItems);
        when(session.getAttribute("menuToUpdate")).thenReturn(Menu.builder().menuId(null).build());
        when(session.getAttribute("user")).thenReturn(user);
        when(restaurantService.getRestaurantByUsername(user.getUsername())).thenReturn(restaurant);
        when(restaurantService.getMenuByRestaurant(restaurant)).thenReturn(expectedMenu);

        // Act
        String viewName = restaurantController.addAllMenuItemsToMenu(session);

        // Assert
        assertEquals("redirect:/showOwnerLoggedInView", viewName);
        verify(session).getAttribute("menuItems");
        verify(session).getAttribute("menuToUpdate");
        verify(session).getAttribute("user");
        verify(restaurantService).getRestaurantByUsername(user.getUsername());
        verify(restaurantService).getMenuByRestaurant(restaurant);
        verify(session).setAttribute("menu", expectedMenu);
        for (MenuItem menuItem : existingMenuItems) {
            verify(restaurantService).addMenuItemToTheMenu(menuItem, expectedMenu);
        }
        verify(session).removeAttribute("menuToUpdate");
        verify(session).removeAttribute("groupedMenuItems");
        verify(session).removeAttribute("menuItems");
    }

    @Test
    public void updateFoodOrderStatusToDelivery_updatesStatusAndRedirects() throws Exception {
        // Arrange
        Long orderId = 1L;

        // Act
        String viewName = restaurantController.updateFoodOrderStatusToDelivery(orderId, session);

        // Assert
        assertEquals("redirect:/showOrdersInProgress", viewName);
        verify(foodOrderService).updateFoodOrderStatus(orderId, FoodOrderStatus.DELIVERED.toString());
        verifyNoMoreInteractions(foodOrderService);
    }

    @Test
    public void showFinishedOrders_RestaurantFound_ReturnsCorrectView() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Model model = mock(Model.class);
        List<FoodOrder> finishedOrders = new ArrayList<>();

        restaurant = Restaurant.builder().build();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");
        SecurityContextHolder.setContext(securityContext);


        when(restaurantService.getRestaurantByUsername("user123")).thenReturn(restaurant);
        when(foodOrderService.getFoodOrdersWithStatus(restaurant.getRestaurantId(), FoodOrderStatus.DELIVERED.toString())).thenReturn(finishedOrders);

        // Act
        String viewName = restaurantController.showFinishedOrders(model);

        // Assert
        assertEquals("finishedOrdersView", viewName);
        verify(securityContext).getAuthentication();
        verify(authentication).getName();

        verify(restaurantService).getRestaurantByUsername("user123");
        verify(foodOrderService).getFoodOrdersWithStatus(restaurant.getRestaurantId(), FoodOrderStatus.DELIVERED.toString());

        verify(model).addAttribute("finishedFoodOrders", finishedOrders);
    }

    @Test
    public void showOrdersInProgress_RestaurantFound_ReturnsCorrectView() {
        // Arrange
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        Model model = mock(Model.class);
        List<FoodOrder> initialFoodOrders = new ArrayList<>();
        List<FoodOrder> filteredFoodOrders = new ArrayList<>();

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("user123");
        SecurityContextHolder.setContext(securityContext);

        Restaurant restaurant = Restaurant.builder()
                .restaurantId(1L)
                .build();
        when(restaurantService.getRestaurantByUsername("user123")).thenReturn(restaurant);
        when(foodOrderService.getFoodOrdersWithStatus(restaurant.getRestaurantId(), FoodOrderStatus.CONFIRMED.toString())).thenReturn(initialFoodOrders);

        when(foodOrderService.getFoodOrders(initialFoodOrders, restaurant)).thenReturn(filteredFoodOrders);

        // Act
        String viewName = restaurantController.showOrdersInProgress(model);

        // Assert
        assertEquals("foodOrdersForRestaurantInProgressView", viewName);
        verify(securityContext).getAuthentication();
        verify(authentication).getName();

        verify(restaurantService).getRestaurantByUsername("user123");
        verify(foodOrderService).getFoodOrdersWithStatus(restaurant.getRestaurantId(), FoodOrderStatus.CONFIRMED.toString());

        verify(foodOrderService).getFoodOrders(initialFoodOrders, restaurant);
        verify(model).addAttribute("foodOrdersInProgress", filteredFoodOrders);
    }

    @Test
    public void changeMenu_withExistingMenuInSession_RetrievesMenuItemsAndDeletesMenu() {
        // Arrange
        Menu existingMenu = Menu.builder().build();
        Set<MenuItem> menuItems = new HashSet<>();
        User user = Mockito.mock(User.class);
        Restaurant restaurant = Restaurant.builder().build();

        when(session.getAttribute("menu")).thenReturn(existingMenu);
        when(restaurantService.getMenuItemsByMenuId(existingMenu)).thenReturn(menuItems);

        // Act
        String viewName = restaurantController.changeMenu(session);

        // Assert
        assertEquals("redirect:/showAddMenuToTheRestaurantView", viewName);

        verify(session).getAttribute("menu");

        verify(restaurantService).getMenuItemsByMenuId(existingMenu);

        verify(restaurantService).deleteMenu(existingMenu);

    }

}