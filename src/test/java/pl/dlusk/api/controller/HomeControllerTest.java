package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.ui.Model;
import pl.dlusk.api.dto.ClientRegisterRequestDTO;
import pl.dlusk.api.dto.OwnerRegisterRequestDTO;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.domain.Roles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HomeControllerTest {
    @InjectMocks
    private HomeController homeController;

    @Mock
    private RestaurantService restaurantService;
    @Mock
    private Model model;
    @Mock
    private HttpSession session;
    @Mock
    private Page<Restaurant> restaurantPage;

    @Test
    public void testHome() {
        // Act
        String viewName = homeController.home();

        // Assert
        assertEquals("homeView", viewName, "The view name should match the expected one.");
    }
    @Test
    void testShowRestaurantsDeliveringOnTheStreet() {
        // Arrange
        String location = "123 Main St";
        int currentPage = 1;
        when(restaurantService.getRestaurantsDeliveringToArea(eq(location), any(Pageable.class))).thenReturn(restaurantPage);
        when(restaurantPage.getTotalPages()).thenReturn(5);

        // Act
        String viewName = homeController.showRestaurantsDeliveringOnTheStreet(location, currentPage, model, session);

        // Assert
        assertEquals("restaurantsDeliveringToGivenArea", viewName);
        verify(model).addAttribute("currentPage", currentPage);
        verify(model).addAttribute("totalPages", 5);
        verify(model).addAttribute("restaurants", restaurantPage);
        verify(model).addAttribute("location", location);
        verify(session, times(1)).setAttribute("location", location);
        verify(restaurantService).getRestaurantsDeliveringToArea(location, PageRequest.of(currentPage, 2));
    }

    @Test
    void testShowRegisterForms() {
        // Arrange
        OwnerRegisterRequestDTO ownerRegisterRequestDTO = OwnerRegisterRequestDTO.builder()
                .name("")
                .surname("")
                .phoneNumber("")
                .nip("")
                .regon("")
                .userDTO(
                        OwnerRegisterRequestDTO.UserDTO.builder()
                                .username("")
                                .password("")
                                .email("")
                                .enabled(true)
                                .role(Roles.OWNER.toString())
                                .build())
                .build();

        ClientRegisterRequestDTO clientRegisterRequestDTO = ClientRegisterRequestDTO.builder()
                .fullName("")
                .phoneNumber("")
                .userDTO(
                        ClientRegisterRequestDTO.UserDTO.builder()
                                .username("")
                                .password("")
                                .email("")
                                .enabled(true)
                                .role(Roles.CLIENT.toString())
                                .build())
                .build();


        // Act
        String returnedView = homeController.showRegisterForms(session, model);

        // Assert
        assertEquals("clientOwnerRegistration", returnedView, "The returned view should be 'clientOwnerRegistration'.");

        verify(model).addAttribute("client", clientRegisterRequestDTO);
        verify(model).addAttribute("owner", ownerRegisterRequestDTO);
        verify(session).setAttribute("owner", ownerRegisterRequestDTO);
        verify(session).setAttribute("client", clientRegisterRequestDTO);
    }
}