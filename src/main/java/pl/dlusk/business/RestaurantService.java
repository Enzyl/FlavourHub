package pl.dlusk.business;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.dlusk.api.dto.RestaurantRegistrationDTO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryStreetEntity;
import pl.dlusk.infrastructure.database.repository.jpa.RestaurantDeliveryStreetJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.RestaurantDeliveryStreetEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class RestaurantService {
    private final RestaurantDeliveryStreetJpaRepository restaurantDeliveryStreetJpaRepository;

    private final RestaurantDAO restaurantDAO;
    private final RestaurantDeliveryStreetEntityMapper restaurantDeliveryStreetEntityMapper;

    public List<Restaurant> getAllRestaurants() {
        return restaurantDAO.findAllRestaurants();
    }

    public Restaurant getRestaurantById(Long restaurantId) {
        return restaurantDAO.findRestaurantById(restaurantId);
    }

    public Restaurant addRestaurant(Restaurant restaurant, RestaurantAddress address, Owner owner) {
        return restaurantDAO.addRestaurant(restaurant, address, owner);
    }


    public Restaurant updateRestaurant(Long restaurantId, Restaurant restaurantDetails) {
        return restaurantDAO.updateRestaurant(restaurantId, restaurantDetails);
    }

    public void deleteRestaurant(Long restaurantId) {
        restaurantDAO.delete(restaurantId);
    }
    public void deleteMenu(Menu menu){
        restaurantDAO.deleteMenu(menu);
    }

    public Menu getMenuByRestaurant(Restaurant restaurant) {
        return restaurantDAO.findMenuRestaurantById(restaurant.getRestaurantId());

    }

    public Page<Restaurant> getRestaurantsDeliveringToArea(String location, Pageable pageable) {
        return restaurantDAO.findRestaurantsDeliveringToArea(location, pageable);
    }

    public List<Review> getReviewsByRestaurantId(Long restaurantId) {
        return restaurantDAO.findReviewsByRestaurantId(restaurantId);

    }

    public Restaurant getRestaurantByUsername(String username) {
        Restaurant restaurantByUsername = restaurantDAO.findRestaurantByUsername(username);
        return restaurantByUsername;
    }

    public void addMenuItemToTheMenu(MenuItem menuItem, Menu menu) {
        log.info("########## RestaurantService ##### addMenuItemToTheMenu #### menuItem: " + menuItem);
        log.info("########## RestaurantService ##### addMenuItemToTheMenu #### menu: " + menu);
        restaurantDAO.saveMenuItem(menuItem, menu);

    }


    public List<RestaurantDeliveryArea> findDeliveryAreaForRestaurant(Long restaurantId) {

        List<RestaurantDeliveryArea> deliveryAreas = restaurantDAO.findDeliveryAreasByRestaurantId(restaurantId);
        log.info("########## RestaurantService ##### findDeliveryAreaForRestaurant #### deliveryAreas: " + deliveryAreas);
        for (int i = 0; i < deliveryAreas.size(); i++) {
            RestaurantDeliveryArea deliveryArea = deliveryAreas.get(i);
            Long restaurantDeliveryStreetId = deliveryArea.getDeliveryStreet().getRestaurantDeliveryStreetId();
            Optional<RestaurantDeliveryStreetEntity> byId = restaurantDeliveryStreetJpaRepository.findById(restaurantDeliveryStreetId);
            if (byId.isPresent()) {
                RestaurantDeliveryArea updatedDeliveryArea = deliveryArea.withDeliveryStreet(restaurantDeliveryStreetEntityMapper.mapFromEntity(byId.get()));
                deliveryAreas.set(i, updatedDeliveryArea);

                log.info("########## RestaurantService ##### findDeliveryAreaForRestaurant #### updatedDeliveryArea: {}", updatedDeliveryArea);
            }
        }


        if (deliveryAreas == null || deliveryAreas.isEmpty()) {
            log.info("No delivery areas found for restaurant with ID: {}", restaurantId);
            return new ArrayList<>();
        }

        return deliveryAreas;
    }

    public void addDeliveryStreetToRestaurant(Long restaurantId, RestaurantDeliveryStreet newDeliveryStreet) {
        log.info("Adding new delivery street to restaurant with ID: {}", restaurantId);


        Restaurant restaurant = restaurantDAO.findRestaurantById(restaurantId);


        restaurantDAO.addDeliveryAreaForRestaurant(restaurantId, newDeliveryStreet);

        log.info("New delivery street added successfully to restaurant with ID: {}", restaurantId);
    }

    public Set<MenuItem> getMenuItemsByMenuId(Menu menu) {
        Set<MenuItem> menuItems = restaurantDAO.findMenuItemsByMenuId(menu.getMenuId());
        return menuItems;

    }

    public Menu getMenuRestaurantById(Long restaurantId) {
        Menu menu = restaurantDAO.findMenuRestaurantById(restaurantId);
        return menu;
    }


    public MenuItem getMenuItemById(Long menuItemId) {
        MenuItem menuItemById = restaurantDAO.findMenuItemById(menuItemId);
        return menuItemById;
    }

    public Menu addMenu(Menu menu) {
        Menu savedMenu = restaurantDAO.save(menu);
        return savedMenu;
    }

    public Restaurant createRestaurant(RestaurantRegistrationDTO restaurantDTO, String imageUrl, Owner owner) {
        RestaurantAddress restaurantAddress = RestaurantAddress.builder().city(restaurantDTO.getAddress().getCity()).postalCode(restaurantDTO.getAddress().getPostalCode()).address(restaurantDTO.getAddress().getAddress()).build();
        return Restaurant.builder().name(restaurantDTO.getName()).description(restaurantDTO.getDescription()).imagePath(imageUrl).address(restaurantAddress).owner(owner).build();
    }

    public Restaurant getOrCreateRestaurant(HttpSession session) {
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        if (restaurant == null) {
            String username = ((FoodOrderingAppUser) session.getAttribute("user")).getUsername();
            restaurant = getRestaurantByUsername(username);
            session.setAttribute("restaurant", restaurant);
        }
        return restaurant;
    }

    public Menu getOrCreateMenu(HttpSession session, Restaurant restaurant) {
        Menu menu = (Menu) session.getAttribute("menuToUpdate");
        if (menu == null) {
            menu = getMenuRestaurantById(restaurant.getRestaurantId());
            session.setAttribute("menu", menu);
        }
        return menu;
    }

    public Restaurant getCurrentRestaurant(HttpSession session) {
        Restaurant restaurant = (Restaurant) session.getAttribute("restaurant");
        if (restaurant == null) {
            String username = (String) session.getAttribute("username");
            restaurant = getRestaurantByUsername(username);
            session.setAttribute("restaurant", restaurant);
        }
        return restaurant;
    }
    public Menu getMenuForRestaurantWithMenuItems(Long restaurantId) throws Exception {

        Menu menu = getMenuRestaurantById(restaurantId);
        if (menu == null) {
            throw new Exception("Menu not found for restaurantId: " + restaurantId);
        }
        Set<MenuItem> menuItems = getMenuItemsByMenuId(menu);
        return menu.withMenuItems(menuItems == null ? new HashSet<>() : menuItems);
    }
}
