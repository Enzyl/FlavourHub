package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.Model;
import pl.dlusk.business.OwnerService;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.MenuItem;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    @Mock
    private OwnerService ownerService;
    @Mock
    private RestaurantService restaurantService;
    @Mock
    private HttpSession session;
    @Mock
    private Model model;

    @InjectMocks
    private OwnerController ownerController;

    @Test
    void showOwnerLoggedInView_UserNotLoggedIn() {
        // Arrange
        when(session.getAttribute("username")).thenReturn(null);

        // Act
        String result = ownerController.showOwnerLoggedInView(session, model);

        // Assert
        assertEquals("redirect:/login", result);
        verify(session, never()).setAttribute(eq("user"), any());
        verify(model, never()).addAttribute(anyString(), any());
    }

    @Test
    void showOwnerLoggedInView_RestaurantNotFound() {
        // Arrange
        String username = "testUser";
        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getUserByUsername(username)).thenReturn(new FoodOrderingAppUser());
        when(restaurantService.getRestaurantByUsername(username)).thenReturn(null);

        // Act
        String result = ownerController.showOwnerLoggedInView(session, model);

        // Assert
        assertEquals("redirect:/showRestaurantRegistrationForm", result);
    }

    @Test
    void showOwnerLoggedInView_NoMenuFound() {
        // Arrange
        String username = "testUser";
        Restaurant restaurant = Restaurant.builder()
                .build();
        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getUserByUsername(username)).thenReturn(new FoodOrderingAppUser());
        when(restaurantService.getRestaurantByUsername(username)).thenReturn(restaurant);
        when(restaurantService.getMenuByRestaurant(restaurant)).thenReturn(null);

        // Act
        String result = ownerController.showOwnerLoggedInView(session, model);

        // Assert
        assertEquals("redirect:/showAddMenuToTheRestaurantView", result);
    }

    @Test
    void showOwnerLoggedInView_NoMenuItemsFound() {
        // Arrange
        String username = "testUser";
        Restaurant restaurant = Restaurant.builder()
                .build();
        Menu menu = Menu.builder().build();
        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getUserByUsername(username)).thenReturn(new FoodOrderingAppUser());
        when(restaurantService.getRestaurantByUsername(username)).thenReturn(restaurant);
        when(restaurantService.getMenuByRestaurant(restaurant)).thenReturn(menu);
        when(restaurantService.getMenuItemsByMenuId(menu)).thenReturn(Collections.emptySet());

        // Act
        String result = ownerController.showOwnerLoggedInView(session, model);

        // Assert
        assertEquals("redirect:/addItemsToTheMenuView", result);
    }

    @Test
    void showOwnerLoggedInView_Success() {
        // Arrange
        String username = "testUser";
        Restaurant restaurant = Restaurant.builder()
                .build();
        Menu menu = Menu.builder().build();
        Set<MenuItem> menuItems = new HashSet<>(Arrays.asList(MenuItem.builder().build()));

        when(session.getAttribute("username")).thenReturn(username);
        when(ownerService.getUserByUsername(username)).thenReturn(new FoodOrderingAppUser());
        when(restaurantService.getRestaurantByUsername(username)).thenReturn(restaurant);
        when(restaurantService.getMenuByRestaurant(restaurant)).thenReturn(menu);
        when(restaurantService.getMenuItemsByMenuId(menu)).thenReturn(menuItems);

        // Act
        String result = ownerController.showOwnerLoggedInView(session, model);

        // Assert
        assertEquals("ownerRestaurantInfo", result);
        verify(model).addAttribute("menu", menu.withMenuItems(menuItems));
        verify(model).addAttribute("restaurant", restaurant);
        verify(model).addAttribute("ownerUsername", username);
    }


}