package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.OrderItemEntity;

import java.util.List;

@Repository

public interface OrderItemsJpaRepository extends JpaRepository<OrderItemEntity, Long> {
    @Query("SELECT oi FROM OrderItemEntity oi WHERE oi.foodOrderEntity.id = :orderId")
    List<OrderItemEntity> findByOrderId(Long orderId);
    List<OrderItemEntity> findByFoodOrderEntityId(Long foodOrderId);

}