package pl.dlusk.business;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;

import java.util.List;

@Service
@AllArgsConstructor
public class RestaurantService {

    private final RestaurantDAO restaurantDAO;

    public List<Restaurant> getAllRestaurants() {
        return restaurantDAO.getAllRestaurants();
    }

    public Restaurant getRestaurantById(Long restaurantId) {
        return restaurantDAO.findById(restaurantId);
    }

    public Restaurant addRestaurant(Restaurant restaurant, RestaurantAddress address, Owner owner) {
        return restaurantDAO.addRestaurant(restaurant, address, owner);
    }


    public Restaurant updateRestaurant(Long restaurantId, Restaurant restaurantDetails) {
        return restaurantDAO.updateRestaurant(restaurantId,restaurantDetails);
    }

    public void deleteRestaurant(Long restaurantId) {
    restaurantDAO.delete(restaurantId);
    }

    public Menu getMenuByRestaurantId(Long restaurantId) {
        return restaurantDAO.getMenuRestaurantById(restaurantId);

    }

    public List<Restaurant> getRestaurantsDeliveringToArea(String streetName) {
        return restaurantDAO.getRestaurantsDeliveringToArea(streetName);

    }

    public List<Review> getReviewsByRestaurantId(Long restaurantId) {
        return restaurantDAO.getReviewsByRestaurantId(restaurantId);

    }

}
