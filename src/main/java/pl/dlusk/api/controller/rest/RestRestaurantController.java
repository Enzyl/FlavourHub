package pl.dlusk.api.controller.rest;

import com.electronwill.nightconfig.core.conversion.Path;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.dlusk.api.dto.MenuDTO;
import pl.dlusk.api.dto.MenuItemDTO;
import pl.dlusk.api.dto.RestaurantDTO;
import pl.dlusk.api.dto.mapper.MenuDTOMapper;
import pl.dlusk.api.dto.mapper.MenuItemDTOMapper;
import pl.dlusk.api.dto.mapper.RestaurantDTOMapper;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.MenuItem;
import pl.dlusk.domain.Restaurant;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/v1/restaurants")
@AllArgsConstructor
public class RestRestaurantController {
    private final RestaurantService restaurantService;
    private final RestaurantDTOMapper restaurantDTOMapper;
    private final MenuDTOMapper menuDTOMapper;
    private final MenuItemDTOMapper menuItemDTOMapper;

    @GetMapping(value = "/{restaurantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<RestaurantDTO> getRestaurant(@PathVariable Long restaurantId) {
        Restaurant restaurant = restaurantService.getRestaurantById(restaurantId);
        if (restaurant == null) {
            return ResponseEntity.notFound().build();
        }
        RestaurantDTO restaurantDTO = restaurantDTOMapper.mapToDTO(restaurant);
        return ResponseEntity.ok(restaurantDTO);
    }

    @GetMapping(value = "", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<RestaurantDTO>> getRestaurants() {
        List<Restaurant> allRestaurants = restaurantService.getAllRestaurants();

        List<RestaurantDTO> restaurantDTOS = mapRestaurantsToDTOs(allRestaurants);

        return ResponseEntity.ok(restaurantDTOS);
    }

    private List<RestaurantDTO> mapRestaurantsToDTOs(List<Restaurant> allRestaurants) {
        List<RestaurantDTO> restaurantsDTO = new ArrayList<>();
        for (Restaurant restaurant : allRestaurants) {
        restaurantsDTO.add(restaurantDTOMapper.mapToDTO(restaurant));
        }
        return restaurantsDTO;
    }

    @GetMapping(value = "/menu/{restaurantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<MenuDTO> getMenuForRestaurant(@PathVariable Long restaurantId) {
        try {
            Menu menuForRestaurantWithMenuItems = restaurantService.getMenuForRestaurantWithMenuItems(restaurantId);
            MenuDTO menuDTO = menuDTOMapper.mapToDTO(menuForRestaurantWithMenuItems);
            return ResponseEntity.ok(menuDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @GetMapping(value = "/menu/menuItems/{restaurantId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Set<MenuItemDTO>> getMenuItemsForRestaurant(@PathVariable Long restaurantId) {
        try {
            Menu menuForRestaurantWithMenuItems = restaurantService.getMenuForRestaurantWithMenuItems(restaurantId);
            Set<MenuItem> menuItems = menuForRestaurantWithMenuItems.getMenuItems();
            Set<MenuItemDTO> menuItemsDTO = menuItemsToMenuItemsDTO(menuItems);
            return ResponseEntity.ok(menuItemsDTO);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Set<MenuItemDTO> menuItemsToMenuItemsDTO(Set<MenuItem> menuItems) {
        Set<MenuItemDTO> menuItemDTOS = new HashSet<>();
        for (MenuItem menuItem : menuItems) {
            menuItemDTOS.add(menuItemDTOMapper.mapToDto(menuItem));
        }
        return menuItemDTOS;
    }

}
