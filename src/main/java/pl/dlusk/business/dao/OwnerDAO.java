package pl.dlusk.business.dao;

import org.springframework.data.jpa.repository.Query;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

import java.util.List;
import java.util.Optional;

public interface OwnerDAO {
    Optional<Owner> findById(Long id);

    List<Owner> findAll();
    Owner saveOwnerWithUserBefore(Owner owner, FoodOrderingAppUser user);
    void deleteById(Long id);
    List<Owner> findBySurname(String surname);
    Optional<Owner> findByNip(String nip);
    Owner findByPhoneNumber(String phoneNumber);
    boolean existsById(Long id);
    boolean existsByNip(String nip);

    Owner saveOwner(Owner owner);
}
