package pl.dlusk.infrastructure.database.repository;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
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
        Optional<FoodOrderingAppUserEntity> existingUser = foodOrderingAppUserJpaRepository.findByUsername(user.getUsername());
        Optional<ClientEntity> existingClientById = clientJpaRepository.findById(client.getClientId());

        if (existingUser.isPresent()) {
            throw new UsernameAlreadyExistsException(user.getUsername());
        }

        if (existingClientById.isPresent()) {
            throw new UsernameAlreadyExistsException(client.getFullName());
        }

        FoodOrderingAppUserEntity userEntity = foodOrderingAppUserEntityMapper.mapToEntity(user);
        FoodOrderingAppUserEntity savedUserEntity = foodOrderingAppUserJpaRepository.save(userEntity);

        ClientEntity clientEntity = clientEntityMapper.mapToEntity(client);

        clientEntity.setUser(savedUserEntity);

        ClientEntity savedNewClient = clientJpaRepository.save(clientEntity);

        return clientEntityMapper.mapFromEntity(savedNewClient).withUser(user);
    }


    @Override
    public Client findById(Long id) {
        return clientJpaRepository.findById(id)
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
        // Pobranie encji użytkownika
        FoodOrderingAppUserEntity userEntity = foodOrderingAppUserJpaRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + userId));

        // Deaktywacja konta użytkownika
        userEntity.setEnabled(false);

        // Zapisanie zmian w bazie danych
        foodOrderingAppUserJpaRepository.save(userEntity);
    }


    public List<FoodOrder> findOrdersByClientId(Long clientId) {
        return clientJpaRepository.findById(clientId)
                .map(ClientEntity::getFoodOrderEntities)
                .orElse(Collections.emptySet())
                .stream()
                .map(foodOrderEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

}
