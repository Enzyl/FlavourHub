package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;

import java.util.List;

@Repository

public interface RestaurantJpaRepository extends JpaRepository<RestaurantEntity, Long> {
    RestaurantEntity findByOwnerEntityId(Long ownerId);

    @Query("SELECT r FROM RestaurantEntity r JOIN r.ownerEntity o JOIN o.user u WHERE u.username = :username")
    RestaurantEntity findRestaurantsByOwnerUsername(@Param("username") String username);


}