package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryAreaEntity;

import java.util.List;

@Repository
public interface RestaurantDeliveryAreaJpaRepository extends JpaRepository<RestaurantDeliveryAreaEntity, Long> {
    @Query("Select rda FROM RestaurantDeliveryAreaEntity rda WHERE rda.deliveryStreet.streetName = :streetName")
    List<RestaurantDeliveryAreaEntity> findByStreetName(String streetName);
}