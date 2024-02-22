package pl.dlusk.business.dao;

import pl.dlusk.domain.*;

import java.util.List;
import java.util.Set;

public interface RestaurantDAO {
    List<Restaurant> getRestaurantsByOwnerId(Long ownerId);
    Restaurant findRestaurantById(Long restaurantId);
    List<Restaurant> getAllRestaurants();
    Restaurant addRestaurant(Restaurant restaurant, RestaurantAddress address, Owner owner);
    Restaurant updateRestaurant(Long restaurantId, Restaurant restaurantDetails);
    void delete(Long restaurantId);
    Menu getMenuRestaurantById(Long restaurantId);
    List<Restaurant> getRestaurantsDeliveringToArea(String streetName);

    List<Review> getReviewsByRestaurantId(Long restaurantId);
    Set<MenuItem> findMenuItemsByMenuId(Long menuId);

}
