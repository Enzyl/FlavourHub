package pl.dlusk.business;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryStreetEntity;
import pl.dlusk.infrastructure.database.repository.jpa.RestaurantDeliveryStreetJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.RestaurantDeliveryStreetEntityMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

    public Menu getMenuByRestaurantId(Long restaurantId) {
        return restaurantDAO.findMenuRestaurantById(restaurantId);

    }

    public List<Restaurant> getRestaurantsDeliveringToArea(String streetName) {
        log.info("########## RestaurantService ##### getRestaurantsDeliveringToArea #### WEJŚCIE: " + streetName);
        return restaurantDAO.findRestaurantsDeliveringToArea(streetName);

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
        // Zakładam, że istnieje metoda findDeliveryAreasByRestaurantId w RestaurantDAO
        List<RestaurantDeliveryArea> deliveryAreas = restaurantDAO.findDeliveryAreasByRestaurantId(restaurantId);
        log.info("########## RestaurantService ##### findDeliveryAreaForRestaurant #### deliveryAreas: " + deliveryAreas);
        for (int i = 0; i < deliveryAreas.size(); i++) {
            RestaurantDeliveryArea deliveryArea = deliveryAreas.get(i);
            Long restaurantDeliveryStreetId = deliveryArea.getDeliveryStreet().getRestaurantDeliveryStreetId();
            Optional<RestaurantDeliveryStreetEntity> byId = restaurantDeliveryStreetJpaRepository.findById(restaurantDeliveryStreetId);
            if (byId.isPresent()) {
                RestaurantDeliveryArea updatedDeliveryArea = deliveryArea
                        .withDeliveryStreet(restaurantDeliveryStreetEntityMapper.mapFromEntity(byId.get()));
                deliveryAreas.set(i, updatedDeliveryArea);  // Zaktualizuj element na liście bezpośrednio

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

        // Znajdowanie restauracji po ID
        Restaurant restaurant = restaurantDAO.findRestaurantById(restaurantId);

        // Ustawienie restauracji dla nowej ulicy dostawy

        // Zapisywanie nowej ulicy dostawy
        restaurantDAO.addDeliveryAreaForRestaurant(restaurantId,newDeliveryStreet);

        log.info("New delivery street added successfully to restaurant with ID: {}", restaurantId);
    }
}
