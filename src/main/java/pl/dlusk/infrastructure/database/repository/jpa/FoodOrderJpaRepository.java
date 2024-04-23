package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FoodOrderJpaRepository extends JpaRepository<FoodOrderEntity, Long> {
    List<FoodOrderEntity> findByClientEntityId(Long clientId);
    List<FoodOrderEntity> findByRestaurantEntityId(Long restaurantId);
    List<FoodOrderEntity> findByStatus(String status);
    List<FoodOrderEntity> findByOrderTimeBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT fo.restaurantEntity FROM FoodOrderEntity fo WHERE fo.id = :foodOrderId")
    RestaurantEntity findRestaurantByFoodOrderId(Long foodOrderId);

    @Modifying
    @Query("UPDATE FoodOrderEntity fo SET fo.status = :status WHERE fo.id = :orderId")
    void updateFoodOrderStatus(@Param("orderId") Long orderId, @Param("status") String status);
    @Query("SELECT o FROM FoodOrderEntity o WHERE o.orderNumber = :orderNumber")
    FoodOrderEntity findByOrderNumber(String orderNumber );

}
