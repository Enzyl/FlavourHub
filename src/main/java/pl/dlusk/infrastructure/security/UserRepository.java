package pl.dlusk.infrastructure.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import pl.dlusk.infrastructure.database.entity.OwnerEntity;
import pl.dlusk.infrastructure.database.repository.jpa.ClientJpaRepository;
import pl.dlusk.infrastructure.database.repository.jpa.OwnerJpaRepository;

import java.util.Optional;
@Repository
@AllArgsConstructor
public class UserRepository implements UserDAO {
    ClientJpaRepository clientJpaRepository;
    UserJpaRepository userJpaRepository;
    UserEntityMapper userEntityMapper;
    private final OwnerJpaRepository ownerJpaRepository;

    @Override
    public User findByUsername(String username) {
        Optional<UserEntity> byUsername = userJpaRepository.findByUsername(username);
        if (byUsername.isEmpty()){
            throw new UsernameNotFoundException("User ["+username+"] not found");
        }
        User user = userEntityMapper.mapFromEntity(byUsername.get());
        return user;
    }

    @Override
    public Long findIdByUsername(String username) {
        Optional<UserEntity> byUsername = userJpaRepository.findByUsername(username);
        Long id = byUsername.get().getId();
        return id;
    }

    @Override
    public User findByClientId(Long clientId) {
        ClientEntity clientEntity = clientJpaRepository.findById(clientId).orElseThrow(
                () -> new RuntimeException("Client not found with id: " + clientId));
        UserEntity userEntity = userJpaRepository.findById(clientEntity.getUser().getId()).orElseThrow(
                () -> new RuntimeException("User not found with id: " + clientEntity.getUser().getId()));
        User user = userEntityMapper.mapFromEntity(userEntity);
        return user;
    }

    @Override
    public User findByOwnerId(Long ownerId) {
        OwnerEntity ownerEntity = ownerJpaRepository.findById(ownerId).orElseThrow(
                () -> new RuntimeException("Owner not found with id: " + ownerId));
        UserEntity userEntity = userJpaRepository.findById(ownerEntity.getUser().getId()).orElseThrow(
                () -> new RuntimeException("User not found with id: " + ownerEntity.getUser().getId()));
        User user = userEntityMapper.mapFromEntity(userEntity);
        return user;
    }
}
