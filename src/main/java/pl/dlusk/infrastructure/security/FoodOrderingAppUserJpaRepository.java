package pl.dlusk.infrastructure.security;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FoodOrderingAppUserJpaRepository extends JpaRepository<FoodOrderingAppUserEntity, Long> {

    Optional<FoodOrderingAppUserEntity> findByUsername(String username);
    Optional<FoodOrderingAppUserEntity> findByEmail(String email);
}
