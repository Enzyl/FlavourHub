package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntity;

import java.util.Optional;

@Repository

public interface ClientJpaRepository extends JpaRepository<ClientEntity, Long> {
    @Query("SELECT c FROM ClientEntity c WHERE c.user.username = :username")
    Optional<ClientEntity> findByUsername(String username);
    @Query("SELECT c FROM ClientEntity c JOIN c.foodOrderEntities fo WHERE fo.id = :foodOrderId")
    Optional<ClientEntity> findByFoodOrderId(Long foodOrderId);

    Optional<ClientEntity> findByUserId(Long userId);
}