package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryAreaEntity;

import java.util.List;

@Repository
public interface RestaurantDeliveryAreaJpaRepository extends JpaRepository<RestaurantDeliveryAreaEntity, Long> {
    @Query("Select rda FROM RestaurantDeliveryAreaEntity rda WHERE rda.deliveryStreet.streetName = :streetName")
    Page<RestaurantDeliveryAreaEntity> findByStreetName(@Param("streetName") String streetName, Pageable pageable);


    @Query("Select rda FROM RestaurantDeliveryAreaEntity rda WHERE rda.restaurantEntity.id = :restaurantId")
    List<RestaurantDeliveryAreaEntity> findByRestaurantEntityId(@Param("restaurantId") Long restaurantId);
}