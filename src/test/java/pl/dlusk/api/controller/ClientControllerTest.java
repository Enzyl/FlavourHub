package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.ui.Model;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;
import pl.dlusk.business.ClientService;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.domain.Client;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class ClientControllerTest {

    @Mock
    private ClientService clientService;

    @Mock
    private RedirectAttributes redirectAttributes;

    @InjectMocks
    private ClientController controller;

    @Mock
    private HttpSession session;

    @Mock
    private Model model;


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
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        Client client = Client.builder().build();

        when(clientService.registerClient(any(Client.class), any(FoodOrderingAppUser.class))).thenReturn(client);

        // Act
        String viewName = controller.registerClient(params, redirectAttributes);

        // Assert
        assertEquals("redirect:/registrationSuccessView", viewName);
        verify(redirectAttributes).addFlashAttribute("registeredClient", client);
        verify(clientService).registerClient(any(Client.class), any(FoodOrderingAppUser.class));
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

        when(clientService.registerClient(any(Client.class), any(FoodOrderingAppUser.class)))
                .thenThrow(new UsernameAlreadyExistsException("Username exists"));

        // Act
        String viewName = controller.registerClient(params, redirectAttributes);

        // Assert
        assertEquals("redirect:/registerClientForm", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Username or email already exists.");
        verify(clientService).registerClient(any(Client.class), any(FoodOrderingAppUser.class));
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

        when(clientService.registerClient(any(Client.class), any(FoodOrderingAppUser.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act
        String viewName = controller.registerClient(params, redirectAttributes);

        // Assert
        assertEquals("redirect:/registerClientForm", viewName);
        verify(redirectAttributes).addFlashAttribute("errorMessage", "Registration failed.");
        verify(clientService).registerClient(any(Client.class), any(FoodOrderingAppUser.class));
    }

    @Test
    void showClientLoggedInView_ReturnsCorrectView() {
        // Arrange
        String expectedUsername = "testUser";
        FoodOrderingAppUser user = new FoodOrderingAppUser();
        user.setUsername(expectedUsername);

        when(session.getAttribute("username")).thenReturn(expectedUsername);
        when(clientService.getUserByUsername(expectedUsername)).thenReturn(user);

        // Act
        String viewName = controller.showClientLoggedInView(model, session);

        // Assert
        assertEquals("clientLoggedInView", viewName);
        verify(session).setAttribute("user", user);
        verify(model).addAttribute("username", expectedUsername);
    }

    @Test
    void showUserProfileView() {
    }

    @Test
    void showAddressForm() {
    }

    @Test
    void submitDeliveryAddress() {
    }

    @Test
    void showConfirmationPage() {
    }

    @Test
    void processOrder() {
    }

    @Test
    void showOrderSummary() {
    }

    @Test
    void showClientOrders() {
    }

    @Test
    void cancelOrder() {
    }

//    @BeforeEach
//    void setUp() {
//    }
//
//    @Test
//    void registerClient() {
//    }
//
//    @Test
//    void showClientLoggedInView() {
//    }
//
//    @Test
//    void showUserProfileView() {
//    }
//
//    @Test
//    void showAddressForm() {
//    }
//
//    @Test
//    void submitDeliveryAddress() {
//    }
//
//    @Test
//    void showConfirmationPage() {
//    }
//
//    @Test
//    void processOrder() {
//    }
//
//    @Test
//    void showOrderSummary() {
//    }
//
//    @Test
//    void showClientOrders() {
//    }
//
//    @Test
//    void cancelOrder() {
//    }
}