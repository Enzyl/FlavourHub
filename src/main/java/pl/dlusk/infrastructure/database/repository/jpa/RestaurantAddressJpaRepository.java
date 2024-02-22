package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.RestaurantAddressEntity;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;

import java.util.List;

@Repository
public interface RestaurantAddressJpaRepository extends JpaRepository<RestaurantAddressEntity, Long> {
}