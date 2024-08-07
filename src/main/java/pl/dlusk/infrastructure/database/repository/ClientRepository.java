package pl.dlusk.infrastructure.database.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.Roles;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import pl.dlusk.infrastructure.database.repository.jpa.ClientJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.ClientEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.FoodOrderEntityMapper;
import pl.dlusk.infrastructure.security.User;
import pl.dlusk.infrastructure.security.UserEntity;
import pl.dlusk.infrastructure.security.UserEntityMapper;
import pl.dlusk.infrastructure.security.UserJpaRepository;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
@Slf4j
@Repository
@AllArgsConstructor
public class ClientRepository implements ClientDAO {
    private final ClientJpaRepository clientJpaRepository;
    private final UserJpaRepository userJpaRepository;
    private final ClientEntityMapper clientEntityMapper;
    private final FoodOrderEntityMapper foodOrderEntityMapper;
    private final UserEntityMapper userEntityMapper;

    @Override
    public Client save(Client client) {
        log.info("########## ClientRepository #### save #  Client: " + client.toString());
        log.info("########## ClientRepository #### save #  FoodOrderingAppUser: " + client.getUser().toString());
        User user = client.getUser();
        Optional<UserEntity> existingUser = userJpaRepository.findByUsername(user.getUsername());
        log.info("########## ClientRepository #### save #  existingUser " + existingUser);
        Optional<ClientEntity> clientByUsername = clientJpaRepository.findByUsername(user.getUsername());

        if (existingUser.isPresent()) {
            log.info("########## ClientRepository #### save #  existingUser.isPresent(): " + existingUser.isPresent());
            throw new UsernameAlreadyExistsException(user.getUsername());
        }

        if (clientByUsername.isPresent()) {
            log.info("########## ClientRepository #### save #  existingClientById.isPresent(): " + clientByUsername.isPresent());
            throw new UsernameAlreadyExistsException(client.getFullName());
        }
        log.info("########## ClientRepository #### save #  ALL OK I GUESS: " );

        UserEntity userEntity = userEntityMapper.mapToEntity(user);
        userEntity.setRole(Roles.CLIENT.toString());
        UserEntity savedUserEntity = userJpaRepository.save(userEntity);

        log.info("########## ClientRepository #### save #  userEntity {} ",userEntity );
        log.info("########## ClientRepository #### save #  userEntity {}",savedUserEntity );

        ClientEntity clientEntity = clientEntityMapper.mapToEntity(client);
        log.info("########## ClientRepository #### save #  clientEntity {}",clientEntity );

        clientEntity.setUser(savedUserEntity);
        log.info("########## ClientRepository #### save #  clientEntity.setUser(savedUserEntity); {}",clientEntity );

        ClientEntity savedNewClient = clientJpaRepository.save(clientEntity);

        log.info("########## ClientRepository #### save #  savedNewClient: {} ",savedNewClient );

        return clientEntityMapper.mapFromEntity(savedNewClient).withUser(user);
    }


    @Override
    public Client findByUserId(Long id) {
        log.info("########## ClientRepo #### findById #  START # id: "+id );

        return clientJpaRepository.findByUserId(id)
                .map(clientEntityMapper::mapFromEntity)
                .orElse(null);
    }

    @Override
    public List<Client> findAll() {
        log.info("########## ClientRepo #### findAll #  START ");

        List<Client> collect = clientJpaRepository.findAll().stream()
                .map(clientEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
        System.out.println(collect);
        return collect;
    }

    @Override
    public void deleteById(Long id) {
        clientJpaRepository.deleteById(id);
    }

    @Override
    public void deactivateAccount(Long userId) {
        UserEntity userEntity = userJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        userEntity.setEnabled(false);
        userJpaRepository.save(userEntity);
    }

    @Override
    public Client findClientByOrderId(Long orderId) {
        ClientEntity clientByFoodOrderId = clientJpaRepository.
                findByFoodOrderId(orderId).orElseThrow(
                () -> new RuntimeException("Client for the order with ID: " + orderId + " not found"));
        return clientEntityMapper.mapFromEntity(clientByFoodOrderId);
    }

    @Override
    public Client findClientByUsername(String username) {
        ClientEntity clientEntity = clientJpaRepository.findByUsername(username).orElseThrow(() -> new RuntimeException("User not found"));
        return clientEntityMapper.mapFromEntity(clientEntity);
    }

    @Override
    public Client findByClientId(Long clientId) {
        ClientEntity clientEntity = clientJpaRepository.findById(clientId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + clientId));
        return clientEntityMapper.mapFromEntity(clientEntity);
    }

    @Override
    public List<FoodOrder> findOrdersByClientId(Long clientId) {

        return clientJpaRepository.findByUserId(clientId)
                .map(ClientEntity::getFoodOrderEntities)
                .orElse(Collections.emptySet())
                .stream()
                .map(foodOrderEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }


}
