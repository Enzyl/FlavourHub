package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.MenuEntity;
import pl.dlusk.infrastructure.database.repository.mapper.MenuEntityMapper;
import org.springframework.data.jpa.repository.Query;

@Repository

public interface MenuJpaRepository extends JpaRepository<MenuEntity, Long> {
    @Query("SELECT m FROM MenuEntity m WHERE m.restaurantEntity.id = :restaurantId")
    MenuEntity findByRestaurantId(Long restaurantId);
}