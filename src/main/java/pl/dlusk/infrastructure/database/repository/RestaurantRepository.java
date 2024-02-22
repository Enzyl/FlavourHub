package pl.dlusk.infrastructure.database.repository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.entity.*;
import pl.dlusk.infrastructure.database.repository.jpa.*;
import pl.dlusk.infrastructure.database.repository.mapper.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@AllArgsConstructor
public class RestaurantRepository implements RestaurantDAO {

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantEntityMapper restaurantEntityMapper;

    private final OwnerJpaRepository ownerJpaRepository;

    private final RestaurantAddressJpaRepository restaurantAddressJpaRepository;
    private final RestaurantAddressEntityMapper restaurantAddressEntityMapper;

    private final MenuJpaRepository menuJpaRepository;
    private final MenuEntityMapper menuEntityMapper;

    private final MenuItemJpaRepository menuItemJpaRepository;
    private final MenuItemEntityMapper menuItemEntityMapper;

    private final RestaurantDeliveryAreaJpaRepository restaurantDeliveryAreaJpaRepository;

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewEntityMapper reviewEntityMapper;

    @Override
    public List<Restaurant> getRestaurantsByOwnerId(Long ownerId) {
        List<RestaurantEntity> restaurantEntities = restaurantJpaRepository.findByOwnerEntityId(ownerId);
        return restaurantEntities.stream()
                .map(restaurantEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Restaurant findRestaurantById(Long restaurantId) {
        log.info("########## RestaurantRepository ##### findRestaurantById #### restaurantId: " + restaurantId );
        RestaurantEntity restaurantEntity = restaurantJpaRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + restaurantId));
        log.info("########## restaurantEntity : " + restaurantEntity);
        return restaurantEntityMapper.mapFromEntity(restaurantEntity);
    }

    @Override
    public List<Restaurant> getAllRestaurants() {
        List<RestaurantEntity> allRestaurantsEntities = restaurantJpaRepository.findAll();
        List<Restaurant> allRestaurantEntities = allRestaurantsEntities.stream()
                .map(restaurantEntityMapper::mapFromEntity)
                .toList();
        return allRestaurantEntities;
    }


    @Override
    public Restaurant addRestaurant(Restaurant restaurant, RestaurantAddress address, Owner owner) {
        Optional<OwnerEntity> ownerByIdOpt = ownerJpaRepository.findById(owner.getOwnerId());
        if (ownerByIdOpt.isEmpty()) {
            throw new RuntimeException("No such owner found with id: " + owner.getOwnerId());
        }

        var restaurantAddressOpt = restaurantAddressJpaRepository.findById(address.getRestaurantAddressId());
        if (restaurantAddressOpt.isEmpty()) {
            throw new RuntimeException("No such address found with id: " + address.getRestaurantAddressId());
        }
        Restaurant restaurantWithOwner = restaurant.withOwner(owner);
        Restaurant restaurantWithOwnerAndAddress = restaurantWithOwner.withAddress(address);

        RestaurantEntity restaurantEntityToSave = restaurantEntityMapper.mapToEntity(restaurantWithOwnerAndAddress);
        RestaurantEntity savedEntity = restaurantJpaRepository.save(restaurantEntityToSave);

        return restaurantEntityMapper.mapFromEntity(savedEntity);
    }

    @Override
    public Restaurant updateRestaurant(Long restaurantId, Restaurant restaurantDetails) {
        return restaurantJpaRepository.findById(restaurantId).map(existingRestaurant -> {
                    if (restaurantDetails.getName() != null) {
                        existingRestaurant.setName(restaurantDetails.getName());
                    }

                    if (restaurantDetails.getDescription() != null) {
                        existingRestaurant.setDescription(restaurantDetails.getDescription());
                    }

                    if (restaurantDetails.getImagePath() != null) {
                        existingRestaurant.setImagePath(restaurantDetails.getImagePath());
                    }

                    // Przykład aktualizacji adresu, zakładając, że masz metodę do mapowania adresu domenowego na encję
                    if (restaurantDetails.getAddress() != null) {

                        RestaurantAddress address = restaurantDetails.getAddress();
                        RestaurantAddressEntity restaurantAddressEntity = restaurantAddressEntityMapper.mapToEntity(address);
                        existingRestaurant.setAddress(restaurantAddressEntity);
                    }

                    // Zapisz zmiany w bazie danych
                    RestaurantEntity updatedRestaurant = restaurantJpaRepository.save(existingRestaurant);

                    // Zamapuj zaktualizowaną encję z powrotem na obiekt domenowy
                    return restaurantEntityMapper.mapFromEntity(updatedRestaurant);
                }
        ).orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id " + restaurantId));
    }

    @Override
    public void delete(Long restaurantId) {
        restaurantJpaRepository.deleteById(restaurantId);
    }

    @Override
    public Menu getMenuRestaurantById(Long restaurantId) {
        log.info("########## RestaurantRepository ##### getMenuRestaurantById #### restaurantId: " + restaurantId);
        MenuEntity menuEntityByRestaurantId = menuJpaRepository.findByRestaurantId(restaurantId);
        log.info("########## menuEntityByRestaurantId : " + menuEntityByRestaurantId.toString());

        return menuEntityMapper.mapFromEntity(menuEntityByRestaurantId);
    }

    @Override
    public List<Restaurant> getRestaurantsDeliveringToArea(String streetName) {
        log.info("########## RestaurantRepository ##### getRestaurantsDeliveringToArea #### WEJŚCIE: " + streetName);

        // Pobierz listę obszarów dostawy (RestaurantDeliveryAreaEntities) dla danej nazwy ulicy
        List<RestaurantDeliveryAreaEntity> deliveryAreas = restaurantDeliveryAreaJpaRepository.findByStreetName(streetName);
        log.info("########## deliveryAreas : " + deliveryAreas);

        // Pobierz unikalne ID restauracji z tych obszarów dostawy
        Set<Long> restaurantIds = deliveryAreas.stream()
                .map(deliveryArea -> deliveryArea.getRestaurantEntity().getId())
                .collect(Collectors.toSet());
        log.info("########## restaurantIds : " + restaurantIds);

        // Pobierz listę restauracji na podstawie tych ID
        List<RestaurantEntity> restaurantEntities = restaurantJpaRepository.findAllById(restaurantIds);
        log.info("########## restaurantEntities : " + restaurantEntities);

        // Zamień encje restauracji na obiekty domenowe
        return restaurantEntities.stream()
                .map(restaurantEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<Review> getReviewsByRestaurantId(Long restaurantId) {
        // Pobierz listę recenzji dla restauracji o podanym ID
        List<ReviewEntity> reviewEntities = reviewJpaRepository.findByRestaurantId(restaurantId);

        // Zamień listę encji ReviewEntity na listę obiektów domenowych Review
        return reviewEntities.stream()
                .map(reviewEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Set<MenuItem> findMenuItemsByMenuId(Long menuId) {
        return menuItemJpaRepository.findByMenuEntityId(menuId).stream()
                .map(menuItemEntityMapper::mapFromEntity)
                .collect(Collectors.toSet());
    }
}

