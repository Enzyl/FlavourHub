package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.DeliveryEntity;
@Repository

public interface DeliveryJpaRepository extends JpaRepository<DeliveryEntity, Long> {
}