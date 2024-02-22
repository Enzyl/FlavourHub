package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.ReviewEntity;

import java.util.List;

@Repository

public interface ReviewJpaRepository extends JpaRepository<ReviewEntity, Long> {
    @Query("SELECT r FROM ReviewEntity r WHERE r.foodOrderEntity.restaurantEntity.id = :restaurantId")
    List<ReviewEntity> findByRestaurantId(Long restaurantId);
}