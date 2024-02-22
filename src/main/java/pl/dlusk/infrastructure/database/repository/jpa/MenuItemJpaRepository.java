package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.MenuItemEntity;

import java.util.List;

@Repository

public interface MenuItemJpaRepository extends JpaRepository<MenuItemEntity, Long> {
    List<MenuItemEntity> findByMenuEntityId(Long menuId);
}