package pl.dlusk.business.dao;

import pl.dlusk.domain.*;

import java.util.List;
import java.util.Set;

public interface RestaurantDAO {
    Restaurant getRestaurantByOwnerId(Long ownerId);

    Restaurant findRestaurantById(Long restaurantId);

    List<Restaurant> findAllRestaurants();

    Restaurant addRestaurant(Restaurant restaurant, RestaurantAddress address, Owner owner);

    Restaurant updateRestaurant(Long restaurantId, Restaurant restaurantDetails);

    void delete(Long restaurantId);

    Menu findMenuRestaurantById(Long restaurantId);

    List<Review> findReviewsByRestaurantId(Long restaurantId);

    Set<MenuItem> findMenuItemsByMenuId(Long menuId);

    MenuItem findMenuItemById(Long menuItemId);

    List<Restaurant> findRestaurantsDeliveringToArea(String streetName);

    Restaurant findRestaurantByUsername(String username);

    Restaurant findRestaurantByFoodOrderId(Long foodOrderId);
    Menu save(Menu menu);
    void saveMenuItem(MenuItem menuItem, Menu menu);
    void deleteMenu(Menu menu);

    List<RestaurantDeliveryArea> findDeliveryAreasByRestaurantId(Long restaurantId);
    void addDeliveryAreaForRestaurant(Long restaurantId, RestaurantDeliveryStreet newDeliveryStreet);
}
