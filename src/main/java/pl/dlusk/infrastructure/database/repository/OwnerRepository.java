package pl.dlusk.infrastructure.database.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.database.entity.OwnerEntity;
import pl.dlusk.infrastructure.database.repository.jpa.OwnerJpaRepository;
import pl.dlusk.infrastructure.database.repository.jpa.RestaurantJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.OwnerEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.RestaurantEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntity;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserJpaRepository;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class OwnerRepository implements OwnerDAO {

    private final OwnerEntityMapper ownerEntityMapper;
    private final OwnerJpaRepository ownerJpaRepository;

    private final FoodOrderingAppUserJpaRepository foodOrderingAppUserJpaRepository;
    private final FoodOrderingAppUserEntityMapper foodOrderingAppUserEntityMapper;

    private final RestaurantJpaRepository restaurantJpaRepository;
    private final RestaurantEntityMapper restaurantEntityMapper;

    @Override
    public Optional<Owner> findById(Long id) {
        return ownerJpaRepository.findById(id).map(ownerEntityMapper::mapFromEntity);
    }

    @Override
    public List<Owner> findAll() {
        return ownerJpaRepository.findAll().stream()
                .map(ownerEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Owner saveOwnerWithUserBefore(Owner owner, FoodOrderingAppUser user) {
        Optional<OwnerEntity> existingOwnerById = ownerJpaRepository.findById(owner.getOwnerId());
        Optional<FoodOrderingAppUserEntity> existingUserByUsername = foodOrderingAppUserJpaRepository.
                findByUsername(user.getUsername());
        if (existingOwnerById.isPresent()) {
            // Rzuć wyjątek, jeśli owner już istnieje
            throw new UsernameAlreadyExistsException(owner.getName());
        }

        if (existingUserByUsername.isPresent()) {
            // Rzuć wyjątek, jeśli użytkownik już istnieje
            throw new UsernameAlreadyExistsException(user.getUsername());
        }
        FoodOrderingAppUserEntity userEntity = foodOrderingAppUserEntityMapper.mapToEntity(user);
        FoodOrderingAppUserEntity savedUserEntity = foodOrderingAppUserJpaRepository.save(userEntity);

        OwnerEntity ownerEntity = ownerEntityMapper.mapToEntity(owner);

        ownerEntity.setUser(userEntity);
        OwnerEntity savedOwnerWithUser = ownerJpaRepository.save(ownerEntity);
        return ownerEntityMapper.mapFromEntity(savedOwnerWithUser).withUser(user);
    }
    @Override
    public Owner saveOwner(Owner owner) {
        OwnerEntity ownerEntity = ownerEntityMapper.mapToEntity(owner);
        ownerJpaRepository.save(ownerEntity);
        return ownerEntityMapper.mapFromEntity(ownerEntity);
    }

    @Override
    public void deleteById(Long ownerToDeleteId) {
        ownerJpaRepository.findById(ownerToDeleteId).ifPresent(ownerEntity -> {
            foodOrderingAppUserJpaRepository.deleteById(ownerEntity.getUser().getId());
            ownerJpaRepository.deleteById(ownerToDeleteId);
        });
    }


    @Override
    public List<Owner> findBySurname(String surname) {
        List<OwnerEntity> findOwnersEntityBySurname = ownerJpaRepository.findBySurname(surname);
        List<Owner> findOwnersBySurname = findOwnersEntityBySurname.stream().map(ownerEntityMapper::mapFromEntity).toList();
        return new ArrayList<>(findOwnersBySurname);
    }

    @Override
    public Optional<Owner> findByNip(String nip) {
        return ownerJpaRepository.findByNip(nip)
                .map(ownerEntityMapper::mapFromEntity);
    }


    @Override
    public Owner findByPhoneNumber(String phoneNumber) {
        OwnerEntity ownerEntity = ownerJpaRepository.findByPhoneNumber(phoneNumber);
        return ownerEntityMapper.mapFromEntity(ownerEntity);
    }

    @Override
    public boolean existsById(Long id) {
        return ownerJpaRepository.findById(id).isPresent();
    }

    @Override
    public boolean existsByNip(String nip) {
        return findByNip(nip).isPresent();
    }

}
