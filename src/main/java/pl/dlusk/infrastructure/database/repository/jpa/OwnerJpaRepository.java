package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import pl.dlusk.infrastructure.database.entity.OwnerEntity;

import java.util.List;
import java.util.Optional;

@Repository

public interface OwnerJpaRepository extends JpaRepository<OwnerEntity, Long> {
    List<OwnerEntity> findBySurname(String surname);
    Optional<OwnerEntity> findByNip(String nip);
    OwnerEntity findByPhoneNumber(String phoneNumber);

    @Query("SELECT o FROM OwnerEntity o JOIN o.user u JOIN RestaurantEntity r ON r.ownerEntity.id = o.id JOIN RestaurantAddressEntity a ON r.address.id = a.id WHERE a.city = :city")
    List<OwnerEntity> findByRestaurantCity(@Param("city") String city);
    @Query("SELECT o FROM OwnerEntity o WHERE o.user.id = :userId")
    OwnerEntity findByUserId(Long userId);

    @Query("SELECT c FROM OwnerEntity c WHERE c.user.username = :username")
    Optional<OwnerEntity> findByUsername(String username);
}