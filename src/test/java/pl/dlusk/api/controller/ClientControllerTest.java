package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.api.dto.DeliveryAddressFormDTO;
import pl.dlusk.business.ClientService;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.UserService;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;
    @Mock
    private UserService userService;

    @Mock
    private FoodOrderService foodOrderService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @Mock
    private BindingResult bindingResult;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;

    @Mock
    private Authentication authentication;


    @InjectMocks
    private ClientController clientController;


    @Test
    void registerClient_Success() {
        // Arrange
        Map<String, String> params = Map.of(
                "user.username", "testUser",
                "user.password", "testPass",
                "user.email", "test@example.com",
                "user.enabled", "true",
                "fullName", "Test User",
                "phoneNumber", "1234567890"
        );

        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username(params.get("user.username"))
                .password(params.get("user.password"))
                .email(params.get("user.email"))
                .enabled(Boolean.parseBoolean(params.get("user.enabled")))
                .build();

        Client client = Client.builder()
                .fullName(params.get("fullName"))
                .phoneNumber(params.get("phoneNumber"))
                .user(user)
                .build();

        when(userService.createUserFromParams(params)).thenReturn(user);
        when(clientService.createClientFromParams(params, user)).thenReturn(client);
        when(clientService.registerClient(client, user)).thenReturn(client);

        // Act
        String viewName = clientController.registerClient(params, redirectAttributes);

        // Assert
        assertEquals("redirect:/registrationSuccessView", viewName);
        verify(redirectAttributes).addFlashAttribute("registeredClient", client);
        verify(clientService).registerClient(client, user);
    }

    @Test
    void registerClient_UsernameAlreadyExistsException() {
        // Arrange
        Map<String, String> params = Map.of(
                "user.username", "existingUser",
                "user.password", "testPass",
                "user.email", "test@example.com",
                "user.enabled", "true",
                "fullName", "Existing User",
                "phoneNumber", "1234567890"
        );
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        Client client = Client.builder().build();

        when(userService.createUserFromParams(params)).thenReturn(user);
        when(clientService.createClientFromParams(params, user)).thenReturn(client);
        when(clientService.registerClient(client, user))
                .thenThrow(new UsernameAlreadyExistsException("Username exists"));

        // Act
        String viewName = clientController.registerClient(params, redirectAttributes);

        // Assert
        assertEquals("redirect:/registerClientForm", viewName);
        verify(redirectAttributes).addFlashAttribute(eq("errorMessage"), eq("Username or email already exists."));
        verify(clientService).registerClient(client, user);
    }

    @Test
    void registerClient_Exception() {
        // Arrange
        Map<String, String> params = Map.of(
                "user.username", "faultyUser",
                "user.password", "badPass",
                "user.email", "faulty@example.com",
                "user.enabled", "true",
                "fullName", "Faulty User",
                "phoneNumber", "9876543210"
        );

        FoodOrderingAppUser mockedUser = new FoodOrderingAppUser();
        Client client = Client.builder().build();

        when(userService.createUserFromParams(params)).thenReturn(mockedUser);
        when(clientService.createClientFromParams(params, mockedUser)).thenReturn(client);

        when(clientService.registerClient(client, mockedUser))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String viewName = clientController.registerClient(params, redirectAttributes);

        // Assert
        assertEquals("redirect:/registerClientForm", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Registration failed.");
        verify(clientService).registerClient(client, mockedUser);
    }

    @Test
    void showClientLoggedInView_ReturnsCorrectView() {
        // Arrange
        HttpSession mockSession = Mockito.mock(HttpSession.class);
        String expectedUsername = "testUser";
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        user.setUsername(expectedUsername);

        Mockito.when(mockSession.getAttribute("username")).thenReturn(expectedUsername);
        Mockito.when(clientService.getUserByUsername(expectedUsername)).thenReturn(user);

        // Act
        String viewName = clientController.showClientLoggedInView(model, mockSession);

        // Assert
        assertEquals("clientLoggedInView", viewName);
        Mockito.verify(mockSession).getAttribute("username");
        Mockito.verify(model).addAttribute("username", expectedUsername);
        Mockito.verify(clientService).getUserByUsername(expectedUsername);
    }


    @Test
    void showUserProfileView_UserAuthenticated() {
        // Arrange
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        user.setUsername("testUser");
        Client client = Client.builder().build();

        when(authentication.getPrincipal()).thenReturn(user);
        when(clientService.getClientByUsername("testUser")).thenReturn(client);

        // Act
        String viewName = clientController.showUserProfileView(model, authentication);

        // Assert
        assertEquals("clientDetails", viewName);
        verify(model).addAttribute("user", user);
        verify(model).addAttribute("client", client);
    }

    @Test
    void showUserProfileView_UserNotAuthenticated() {
        // Arrange
        when(authentication.getPrincipal()).thenReturn(null);

        // Act
        String viewName = clientController.showUserProfileView(model, authentication);

        // Assert
        assertEquals("redirect:/login", viewName);
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void showUserProfileView_NoClientFound() {
        // Arrange
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        user.setUsername("testUser");

        when(authentication.getPrincipal()).thenReturn(user);
        when(clientService.getClientByUsername("testUser")).thenReturn(null);

        // Act
        String viewName = clientController.showUserProfileView(model, authentication);

        // Assert
        assertEquals("clientDetails", viewName);
        verify(model).addAttribute("errorMessage", "No client profile available.");
    }

    @Test
    void submitDeliveryAddress_Success() {
        // Arrange
        DeliveryAddressFormDTO form = new DeliveryAddressFormDTO(
                "Main Street",
                "123",
                "42",
                "10000",
                "TestCity",
                "Leave at door");
        when(bindingResult.hasErrors()).thenReturn(false);

        // Mocking session behavior
        doAnswer(invocation -> {
            String key = invocation.getArgument(0);
            Object value = invocation.getArgument(1);
            if ("delivery".equals(key) && value instanceof Delivery) {
                Delivery delivery = (Delivery) value;
                assertEquals("Main Street 123, Apt. 42, 10000 TestCity, Leave at door", delivery.getDeliveryAddress());
            }
            return null;
        }).when(session).setAttribute(anyString(), any());

        // Act
        String result = clientController.submitDeliveryAddress(form, bindingResult, session);

        // Assert
        assertEquals("redirect:/confirmationPage", result);
        verify(bindingResult).hasErrors();
        verify(session).setAttribute(eq("delivery"), any(Delivery.class));
    }


    @Test
    void submitDeliveryAddress_HasErrors() {
        // Arrange
        DeliveryAddressFormDTO form = new DeliveryAddressFormDTO("Main Street", "123", "42", "10000", "TestCity", "Leave at door");
        when(bindingResult.hasErrors()).thenReturn(true);

        // Act
        String result = clientController.submitDeliveryAddress(form, bindingResult, session);

        // Assert
        assertEquals("deliveryAddressView", result);
        assertNull(session.getAttribute("delivery"));
        verify(bindingResult).hasErrors();
    }

    @Test
    void processOrder_Success() {
        // Arrange
        String expectedUniqueFoodNumber = "12345";
        when(foodOrderService.processOrder(session)).thenReturn(expectedUniqueFoodNumber);

        // Act
        String viewName = clientController.processOrder(session, redirectAttributes);

        // Assert
        assertEquals("redirect:/showOrderSummary", viewName);
        verify(redirectAttributes).addFlashAttribute("successMessage", "Order processed successfully!");
        verify(foodOrderService).processOrder(session);
    }

    @Test
    void processOrder_Failure() {
        // Arrange
        String errorMessage = "Error processing order";
        when(foodOrderService.processOrder(session)).thenThrow(new RuntimeException(errorMessage));

        // Act
        String viewName = clientController.processOrder(session, redirectAttributes);

        // Assert
        assertEquals("redirect:/orderFailed", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Error processing order: " + errorMessage);
        verify(foodOrderService).processOrder(session);
    }

    @Test
    void showOrderSummary_NoOrderFound() {
        // Arrange
        session.setAttribute("uniqueFoodNumber", null);

        // Act
        String viewName = clientController.showOrderSummary(session, model);

        // Assert
        assertEquals("errorPage", viewName);
        verify(model).addAttribute("errorMessage", "No order found to display.");
        verify(foodOrderService, never()).showOrderSummary(any());
    }

    @Test
    void showOrderSummary_OrderNotFoundInService() {
        // Arrange
        String uniqueFoodNumber = "12345";
        when(session.getAttribute("uniqueFoodNumber")).thenReturn(uniqueFoodNumber);
        when(foodOrderService.showOrderSummary(uniqueFoodNumber)).thenReturn(null);

        // Act
        String viewName = clientController.showOrderSummary(session, model);

        // Assert
        assertEquals("errorPage", viewName);
        verify(model).addAttribute("errorMessage", "Order details could not be retrieved.");
        verify(foodOrderService).showOrderSummary(uniqueFoodNumber);
    }

    @Test
    void showOrderSummary_Success() {
        // Arrange
        String uniqueFoodNumber = "12345";
        FoodOrder foodOrder = FoodOrder.builder().build();
        when(session.getAttribute("uniqueFoodNumber")).thenReturn(uniqueFoodNumber);
        when(foodOrderService.showOrderSummary(uniqueFoodNumber)).thenReturn(foodOrder);

        System.out.println("Unique Food Number from session: " + session.getAttribute("uniqueFoodNumber"));
        System.out.println("Food Order from service: " + foodOrderService.showOrderSummary(uniqueFoodNumber));

        // Act
        String viewName = clientController.showOrderSummary(session, model);

        // Assert
        assertEquals("orderSummaryView", viewName, "The view should be 'orderSummaryView'");
        verify(model).addAttribute("foodOrderWithOrderItems", foodOrder);
        verify(foodOrderService, times(2)).showOrderSummary(uniqueFoodNumber);

    }

    @Test
    void showClientOrders_UserNotFound() {
        // Arrange
        when(session.getAttribute("user")).thenReturn(null);

        // Act
        String viewName = clientController.showClientOrders(session, model);

        // Assert
        assertEquals("loginView", viewName);
        verify(model).addAttribute("errorMessage", "User not found. Please login again.");
    }

    @Test
    void showClientOrders_Success() {
        // Arrange
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        user.setUsername("testUser");
        ClientOrderHistory clientOrderHistory = ClientOrderHistory.builder().build();
        when(session.getAttribute("user")).thenReturn(user);
        when(clientService.getClientOrderHistory("testUser")).thenReturn(clientOrderHistory);

        // Act
        String viewName = clientController.showClientOrders(session, model);

        // Assert
        assertEquals("userOrders", viewName);
        verify(model).addAttribute("clientOrderHistory", clientOrderHistory);
    }

    @Test
    void showClientOrders_Exception() {
        // Arrange
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        user.setUsername("testUser");
        when(session.getAttribute("user")).thenReturn(user);
        when(clientService.getClientOrderHistory("testUser")).thenThrow(new RuntimeException("Database error"));

        // Act
        String viewName = clientController.showClientOrders(session, model);

        // Assert
        assertEquals("errorPage", viewName);
        verify(model).addAttribute("errorMessage", "Unable to retrieve orders at this time.");
    }

    @Test
    void cancelOrder_SuccessfulCancellation() {
        // Arrange
        Long orderId = 1L;
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(10);
        FoodOrder order = FoodOrder.builder().orderTime(orderTime).build();

        when(foodOrderService.getFoodOrderById(orderId)).thenReturn(order);

        // Act
        String viewName = clientController.cancelOrder(orderId, redirectAttributes);

        // Assert
        assertEquals("redirect:/userOrders", viewName);
        verify(foodOrderService).updateFoodOrderStatus(orderId, FoodOrderStatus.CANCELLED.toString());
        verify(redirectAttributes).addFlashAttribute("successMessage", "Order has been cancelled successfully.");
    }

    @Test
    void cancelOrder_FailedDueToTimeConstraint() {
        // Arrange
        Long orderId = 1L;
        LocalDateTime orderTime = LocalDateTime.now().minusMinutes(25);
        FoodOrder order = FoodOrder.builder().orderTime(orderTime).build();

        when(foodOrderService.getFoodOrderById(orderId)).thenReturn(order);

        // Act
        String viewName = clientController.cancelOrder(orderId, redirectAttributes);

        // Assert
        assertEquals("redirect:/userOrders", viewName);
        verify(foodOrderService, never()).updateFoodOrderStatus(eq(orderId), any());
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Order cannot be cancelled after 20 minutes.");
    }

    @Test
    public void testCreateUserFromParams_withValidParams() {
        Map<String, String> params = new HashMap<>();
        params.put("user.username", "testUser");
        params.put("user.password", "password123");
        params.put("user.email", "test@example.com");
        params.put("user.enabled", "true");
        FoodOrderingAppUser expectedUser = FoodOrderingAppUser.builder()
                .username("testUser")
                .password("password123")
                .email("test@example.com")
                .enabled(true)
                .role(Roles.CLIENT.toString())
                .build();

        when(userService.createUserFromParams(params)).thenReturn(expectedUser);

        FoodOrderingAppUser user = userService.createUserFromParams(params);

        assertEquals("testUser", user.getUsername());
        assertEquals("password123", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.isEnabled());
        assertEquals(Roles.CLIENT.toString(), user.getRole());
    }

    @Test
    public void testCreateUserFromParams_withMissingParams() {
        Map<String, String> params = new HashMap<>();
        params.put("user.username", "testUser");
        FoodOrderingAppUser expectedUser = FoodOrderingAppUser.builder()
                .username("testUser")
                .password(null)
                .email(null)
                .enabled(false)
                .role(Roles.CLIENT.toString())
                .build();

        when(userService.createUserFromParams(params)).thenReturn(expectedUser);
        FoodOrderingAppUser user = userService.createUserFromParams(params);

        assertEquals("testUser", user.getUsername());
        assertNull(user.getPassword());
        assertNull(user.getEmail());
        assertFalse(user.isEnabled());
    }

    @Test
    public void testCreateClientFromParams_withValidParams() {
        Map<String, String> params = new HashMap<>();
        params.put("fullName", "John Doe");
        params.put("phoneNumber", "1234567890");
        FoodOrderingAppUser expectedUser = FoodOrderingAppUser.builder()
                .username("testUser")
                .password(null)
                .email(null)
                .enabled(false)
                .role(Roles.CLIENT.toString())
                .build();

        Client expectedClient = Client.builder()
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .user(expectedUser)
                .build();
        when(clientService.createClientFromParams(params,expectedUser)).thenReturn(expectedClient);

        Client client = clientService.createClientFromParams(params, expectedUser);

        assertEquals("John Doe", client.getFullName());
        assertEquals("1234567890", client.getPhoneNumber());
        assertEquals(expectedUser, client.getUser());
    }

}