package pl.dlusk.infrastructure.database.repository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.entity.*;
import pl.dlusk.infrastructure.database.repository.jpa.*;
import pl.dlusk.infrastructure.database.repository.mapper.*;

import java.util.Collections;
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

    private final RestaurantAddressJpaRepository restaurantAddressJpaRepository;
    private final RestaurantAddressEntityMapper restaurantAddressEntityMapper;

    private final OwnerJpaRepository ownerJpaRepository;
    private final OwnerEntityMapper ownerEntityMapper;

    private final MenuJpaRepository menuJpaRepository;
    private final MenuEntityMapper menuEntityMapper;

    private final MenuItemJpaRepository menuItemJpaRepository;
    private final MenuItemEntityMapper menuItemEntityMapper;

    private final RestaurantDeliveryAreaJpaRepository restaurantDeliveryAreaJpaRepository;
    private final RestaurantDeliveryAreaEntityMapper restaurantDeliveryAreaEntityMapper;

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewEntityMapper reviewEntityMapper;

    private final FoodOrderJpaRepository foodOrderJpaRepository;
    private final FoodOrderEntityMapper foodOrderEntityMapper;

    private final RestaurantDeliveryStreetEntityMapper restaurantDeliveryStreetEntityMapper;
    private final RestaurantDeliveryStreetJpaRepository restaurantDeliveryStreetJpaRepository;

    @Override
    public Restaurant getRestaurantByOwnerId(Long ownerId) {
        RestaurantEntity byOwnerEntityId = restaurantJpaRepository.findByOwnerEntityId(ownerId);
        return restaurantEntityMapper.mapFromEntity(byOwnerEntityId);
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
    public List<Restaurant> findAllRestaurants() {
        List<RestaurantEntity> allRestaurantsEntities = restaurantJpaRepository.findAll();
        List<Restaurant> allRestaurantEntities = allRestaurantsEntities.stream()
                .map(restaurantEntityMapper::mapFromEntity)
                .toList();
        return allRestaurantEntities;
    }


    @Override
    public Restaurant addRestaurant(Restaurant restaurant, RestaurantAddress address, Owner owner) {
        log.info("########## RestaurantRepository ##### addRestaurant # START");

        var restaurantAddressOpt = restaurantAddressJpaRepository
                .findByCityPostalCodeAndAddress(address.getCity(), address.getPostalCode(), address.getAddress());
        log.info("########## RestaurantRepository ##### addRestaurant #### restaurantAddressOpt: " + restaurantAddressOpt);

        if (restaurantAddressOpt.isPresent()) {
            throw new RuntimeException("The address with id {} is already being used " + address.getRestaurantAddressId());
        }


        RestaurantAddressEntity restaurantAddressEntity = restaurantAddressEntityMapper.mapToEntity(address);
        restaurantAddressEntity = restaurantAddressJpaRepository.save(restaurantAddressEntity);

        OwnerEntity ownerEntity = ownerEntityMapper.mapToEntity(owner);

        RestaurantEntity restaurantEntity = restaurantEntityMapper.mapToEntity(restaurant);
        restaurantEntity.setOwnerEntity(ownerEntity);
        restaurantEntity.setAddress(restaurantAddressEntity);

        RestaurantEntity savedEntity = restaurantJpaRepository.save(restaurantEntity);
        Restaurant restaurantToReturn = restaurantEntityMapper.mapFromEntity(savedEntity);
        log.info("########## RestaurantRepository ##### addRestaurant #### restaurantToReturn: " + restaurantToReturn);
        return restaurantToReturn;
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


                    if (restaurantDetails.getAddress() != null) {

                        RestaurantAddress address = restaurantDetails.getAddress();
                        RestaurantAddressEntity restaurantAddressEntity = restaurantAddressEntityMapper.mapToEntity(address);
                        existingRestaurant.setAddress(restaurantAddressEntity);
                    }


                    RestaurantEntity updatedRestaurant = restaurantJpaRepository.save(existingRestaurant);


                    return restaurantEntityMapper.mapFromEntity(updatedRestaurant);
                }
        ).orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id " + restaurantId));
    }

    @Override
    public void delete(Long restaurantId) {
        restaurantJpaRepository.deleteById(restaurantId);
    }

    @Override
    public Menu findMenuRestaurantById(Long restaurantId) {
        log.info("########## RestaurantRepository ##### getMenuRestaurantById #### restaurantId: " + restaurantId);
        MenuEntity menuEntityByRestaurantId = menuJpaRepository.findByRestaurantId(restaurantId);
        log.info("########## RestaurantRepository ##### menuEntityByRestaurantId : " + menuEntityByRestaurantId);

        return menuEntityMapper.mapFromEntity(menuEntityByRestaurantId);
    }

    @Override
    public Page<Restaurant> findRestaurantsDeliveringToArea(String streetName, Pageable pageable) {
        log.info("########## RestaurantRepository ##### getRestaurantsDeliveringToArea #### WEJŚCIE: " + streetName);


        Page<RestaurantDeliveryAreaEntity> deliveryAreas = restaurantDeliveryAreaJpaRepository.findByStreetName(streetName, pageable);
        log.info("########## deliveryAreas : " + deliveryAreas);


        Set<Long> restaurantIds = deliveryAreas.getContent().stream()
                .map(deliveryArea -> deliveryArea.getRestaurantEntity().getId())
                .collect(Collectors.toSet());
        log.info("########## restaurantIds : " + restaurantIds);


        List<RestaurantEntity> restaurantEntities = restaurantJpaRepository.findAllById(restaurantIds);
        log.info("########## restaurantEntities : " + restaurantEntities);


        return new PageImpl<>(restaurantEntities.stream()
                .map(restaurantEntityMapper::mapFromEntity)
                .collect(Collectors.toList()), pageable, deliveryAreas.getTotalElements());
    }

    @Override
    public List<Review> findReviewsByRestaurantId(Long restaurantId) {

        List<ReviewEntity> reviewEntities = reviewJpaRepository.findByRestaurantId(restaurantId);


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

    @Override
    public MenuItem findMenuItemById(Long menuItemId) {
        Optional<MenuItemEntity> byId = menuItemJpaRepository.findById(menuItemId);
        MenuItemEntity menuItemEntity = byId.orElseThrow();
        return menuItemEntityMapper.mapFromEntity(menuItemEntity);
    }

    @Override
    public Restaurant findRestaurantByUsername(String username) {
        RestaurantEntity restaurantsByOwnerUsername = restaurantJpaRepository.findRestaurantsByOwnerUsername(username);
        return restaurantEntityMapper.mapFromEntity(restaurantsByOwnerUsername);
    }

    @Override
    public Restaurant findRestaurantByFoodOrderId(Long foodOrderId) {
        RestaurantEntity restaurantByFoodOrderId = foodOrderJpaRepository.findRestaurantByFoodOrderId(foodOrderId);
        return restaurantEntityMapper.mapFromEntity(restaurantByFoodOrderId);
    }

    @Override
    public Menu save(Menu menu) {
        log.info("########## RestaurantRepository ##### save # menu: {}", menu);

        MenuEntity menuEntity = menuEntityMapper.mapToEntity(menu);


        RestaurantEntity restaurantEntity = restaurantJpaRepository.findById(menu.getRestaurant().getRestaurantId())
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with id: " + menu.getRestaurant().getRestaurantId()));

        menuEntity.setRestaurantEntity(restaurantEntity);
        menuJpaRepository.save(menuEntity);

        log.info("########## RestaurantRepository ##### save # savedMenu: {}", menuEntity);
        return menuEntityMapper.mapFromEntity(menuEntity);
    }


    @Override
    public void saveMenuItem(MenuItem menuItem, Menu menu) {
        log.info("########## RestaurantRepository ##### saveMenuItem # START");
        log.info("########## RestaurantRepository ##### saveMenuItem # menuItem: {}", menuItem);
        log.info("########## RestaurantRepository ##### saveMenuItem # menu: {}", menu);

        MenuEntity menuEntity = menuJpaRepository
                .findById(menu.getMenuId())
                .orElseGet(() -> menuJpaRepository.save(menuEntityMapper.mapToEntity(menu)));


        MenuItemEntity menuItemEntity = menuItemEntityMapper.mapToEntity(menuItem);
        menuItemEntity.setMenuEntity(menuEntity);

        menuItemJpaRepository.save(menuItemEntity);
        log.info("########## RestaurantRepository ##### saveMenuItem # savedMenuItemEntity: {}", menuItemEntity);
    }


    @Override
    public void deleteMenu(Menu menu) {
        log.info("########## RestaurantRepository ##### deleteMenu # menu: {}",menu);
        Set<MenuItem> menuItems = menu.getMenuItems();
        for (MenuItem menuItem : menuItems) {
            MenuItemEntity menuItemEntity = menuItemEntityMapper.mapToEntity(menuItem);
            menuItemJpaRepository.delete(menuItemEntity);

        }
        MenuEntity menuEntity = menuEntityMapper.mapToEntity(menu);
        menuJpaRepository.delete(menuEntity);
        log.info("########## RestaurantRepository ##### deleteMenu # menuEntity: {}",menuEntity);

    }

    @Override
    public List<RestaurantDeliveryArea> findDeliveryAreasByRestaurantId(Long restaurantId) {
        List<RestaurantDeliveryAreaEntity> deliveryAreaEntities = restaurantDeliveryAreaJpaRepository.findByRestaurantEntityId(restaurantId);

        if (deliveryAreaEntities.isEmpty()) {
            log.info("No delivery areas found for restaurant with ID: {}", restaurantId);
            return Collections.emptyList();
        }

        return deliveryAreaEntities.stream()
                .map(restaurantDeliveryAreaEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void addDeliveryAreaForRestaurant(Long restaurantId, RestaurantDeliveryStreet newDeliveryStreet) {
        log.info("########## RestaurantRepository ##### addDeliveryAreaForRestaurant # START: {}");


        RestaurantEntity restaurantEntity = restaurantJpaRepository.findById(restaurantId)
                .orElseThrow(() -> new EntityNotFoundException("Restaurant not found with ID: " + restaurantId));


        RestaurantDeliveryStreetEntity deliveryStreetEntity = restaurantDeliveryStreetEntityMapper.mapToEntity(newDeliveryStreet);
        RestaurantDeliveryStreetEntity savedDeliveryStreetEntity = restaurantDeliveryStreetJpaRepository.save(deliveryStreetEntity);


        RestaurantDeliveryAreaEntity newDeliveryArea = new RestaurantDeliveryAreaEntity();
        newDeliveryArea.setRestaurantEntity(restaurantEntity);
        newDeliveryArea.setDeliveryStreet(savedDeliveryStreetEntity);


        restaurantDeliveryAreaJpaRepository.save(newDeliveryArea);

        log.info("########## RestaurantRepository ##### addDeliveryAreaForRestaurant # FINISHED: Area added for restaurantId: {}, street: {}", restaurantId, newDeliveryStreet.getStreetName());
    }



}

