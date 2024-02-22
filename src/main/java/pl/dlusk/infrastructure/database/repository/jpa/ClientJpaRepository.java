package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
@Repository

public interface ClientJpaRepository extends JpaRepository<ClientEntity, Long> {
}