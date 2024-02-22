package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository

public interface FoodOrderJpaRepository extends JpaRepository<FoodOrderEntity, Long> {
    List<FoodOrderEntity> findByClientEntityId(Long clientId);
    List<FoodOrderEntity> findByRestaurantEntityId(Long restaurantId);
    List<FoodOrderEntity> findByStatus(String status);
    List<FoodOrderEntity> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);
}