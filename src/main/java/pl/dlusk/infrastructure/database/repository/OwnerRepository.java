package pl.dlusk.infrastructure.database.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.database.entity.OwnerEntity;
import pl.dlusk.infrastructure.database.repository.jpa.OwnerJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.OwnerEntityMapper;
import pl.dlusk.infrastructure.security.User;
import pl.dlusk.infrastructure.security.UserEntity;
import pl.dlusk.infrastructure.security.UserEntityMapper;
import pl.dlusk.infrastructure.security.UserJpaRepository;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Repository
@AllArgsConstructor
public class OwnerRepository implements OwnerDAO {

    private final OwnerEntityMapper ownerEntityMapper;
    private final OwnerJpaRepository ownerJpaRepository;

    private final UserJpaRepository userJpaRepository;
    private final UserEntityMapper userEntityMapper;


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
    public Owner saveOwnerWithUserBefore(Owner owner, User user) {
        log.info("########## OwnerRepository #### saveOwnerWithUserBefore START");
        log.info("########## OwnerRepository #### saveOwnerWithUserBefore owner {}",owner);
        log.info("########## OwnerRepository #### saveOwnerWithUserBefore user {}",user);
        Optional<OwnerEntity> existingOwnerByNIP = ownerJpaRepository.findByNip(owner.getNip());
        log.info("########## OwnerRepository #### existingOwnerByNIP {}",existingOwnerByNIP);
        Optional<UserEntity> existingUserByEmail = userJpaRepository.findByEmail(user.getEmail());
        log.info("########## OwnerRepository #### existingUserByEmail {}",existingUserByEmail);
        if (existingOwnerByNIP.isPresent()) {
            log.info("########## OwnerRepository #### existingOwnerByNIP.isPresent() {}",existingOwnerByNIP.isPresent());
            throw new UsernameAlreadyExistsException(owner.getName());
        }

        if (existingUserByEmail.isPresent()) {
            log.info("########## OwnerRepository #### existingUserByEmail.isPresent() {}",existingUserByEmail.isPresent());
            throw new UsernameAlreadyExistsException(user.getUsername());
        }
        UserEntity userEntity = userEntityMapper.mapToEntity(user);
        UserEntity savedUserEntity = userJpaRepository.save(userEntity);
        log.info("########## OwnerRepository #### savedUserEntity {}",savedUserEntity);

        OwnerEntity ownerEntity = ownerEntityMapper.mapToEntity(owner);

        ownerEntity.setUser(userEntity);
        OwnerEntity savedOwnerWithUser = ownerJpaRepository.save(ownerEntity);
        log.info("########## OwnerService #### registerOwner FINISH with savedOwnerWithUser: {}",savedOwnerWithUser);
        return ownerEntityMapper.mapFromEntity(savedOwnerWithUser).withUser(user);
    }
    @Override
    public Owner saveOwner(Owner owner) {
        OwnerEntity ownerEntity = ownerEntityMapper.mapToEntity(owner);
        ownerJpaRepository.save(ownerEntity);
        return ownerEntityMapper.mapFromEntity(ownerEntity);
    }

    @Override
    public Owner findByUserId(Long id) {
        OwnerEntity byUserId = ownerJpaRepository.findByUserId(id);
        return ownerEntityMapper.mapFromEntity(byUserId);
    }

    @Override
    public void deleteById(Long ownerToDeleteId) {
        ownerJpaRepository.findById(ownerToDeleteId).ifPresent(ownerEntity -> {
            userJpaRepository.deleteById(ownerEntity.getUser().getId());
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
    public Owner findByUsername(String username) {
        log.info("########## OwnerRepository #### findByUsername #  username   " + username);
        OwnerEntity ownerEntity = ownerJpaRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Owner not found for the username: " + username));

        return ownerEntityMapper.mapFromEntity(ownerEntity);
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
