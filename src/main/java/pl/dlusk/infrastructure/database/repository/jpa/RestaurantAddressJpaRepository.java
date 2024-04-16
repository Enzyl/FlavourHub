package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.RestaurantAddressEntity;

import java.util.Optional;

@Repository
public interface RestaurantAddressJpaRepository extends JpaRepository<RestaurantAddressEntity, Long> {
    @Query("SELECT rae FROM RestaurantAddressEntity rae " +
            "WHERE rae.city = :cityName " +
            "AND rae.postalCode = :postalCode " +
            "AND rae.address = :address")
    Optional<RestaurantAddressEntity> findByCityPostalCodeAndAddress(String cityName, String postalCode, String address);
}