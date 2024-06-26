package pl.dlusk.business.dao;

import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.security.User;

import java.util.List;
import java.util.Optional;

public interface OwnerDAO {
    Optional<Owner> findById(Long id);

    List<Owner> findAll();
    Owner saveOwnerWithUserBefore(Owner owner, User user);
    void deleteById(Long id);
    List<Owner> findBySurname(String surname);
    Owner findByUsername(String username);
    Optional<Owner> findByNip(String nip);
    Owner findByPhoneNumber(String phoneNumber);
    boolean existsById(Long id);
    boolean existsByNip(String nip);

    Owner saveOwner(Owner owner);
    Owner findByUserId(Long id);
}
