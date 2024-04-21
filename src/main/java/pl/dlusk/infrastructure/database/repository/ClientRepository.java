package pl.dlusk.infrastructure.database.repository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import pl.dlusk.infrastructure.database.repository.jpa.ClientJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.ClientEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.FoodOrderEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntity;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserJpaRepository;
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
    private final FoodOrderingAppUserJpaRepository foodOrderingAppUserJpaRepository; // Dodano UserRepository
    private final ClientEntityMapper clientEntityMapper;
    private final FoodOrderEntityMapper foodOrderEntityMapper;
    private final FoodOrderingAppUserEntityMapper foodOrderingAppUserEntityMapper;

    @Override
    public Client save(Client client, FoodOrderingAppUser user) {
        log.info("########## ClientRepository #### save #  Client: " + client.toString());
        log.info("########## ClientRepository #### save #  FoodOrderingAppUser: " + user.toString());

        Optional<FoodOrderingAppUserEntity> existingUser = foodOrderingAppUserJpaRepository.findByUsername(user.getUsername());
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

        FoodOrderingAppUserEntity userEntity = foodOrderingAppUserEntityMapper.mapToEntity(user);
        FoodOrderingAppUserEntity savedUserEntity = foodOrderingAppUserJpaRepository.save(userEntity);

        ClientEntity clientEntity = clientEntityMapper.mapToEntity(client);

        clientEntity.setUser(savedUserEntity);

        ClientEntity savedNewClient = clientJpaRepository.save(clientEntity);


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
        return clientJpaRepository.findAll().stream()
                .map(clientEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        clientJpaRepository.deleteById(id);
    }

    @Override
    public void deactivateAccount(Long userId) {
        FoodOrderingAppUserEntity userEntity = foodOrderingAppUserJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));
        userEntity.setEnabled(false);
        foodOrderingAppUserJpaRepository.save(userEntity);
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
    public List<FoodOrder> findOrdersByClientId(Long clientId) {
        return clientJpaRepository.findByUserId(clientId)
                .map(ClientEntity::getFoodOrderEntities)
                .orElse(Collections.emptySet())
                .stream()
                .map(foodOrderEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }


}
