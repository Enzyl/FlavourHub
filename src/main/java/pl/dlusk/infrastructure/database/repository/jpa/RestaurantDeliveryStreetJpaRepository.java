package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryStreetEntity;
@Repository

public interface RestaurantDeliveryStreetJpaRepository extends JpaRepository<RestaurantDeliveryStreetEntity, Long> {
}