package pl.dlusk.infrastructure.database.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.FoodOrderStatus;
import pl.dlusk.domain.Roles;
import pl.dlusk.infrastructure.database.entity.ClientEntity;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
import pl.dlusk.infrastructure.database.repository.jpa.ClientJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.ClientEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.FoodOrderEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntity;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserJpaRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class ClientRepositoryTest {
    @Mock
    private ClientJpaRepository clientJpaRepository;
    @Mock
    private FoodOrderEntityMapper foodOrderEntityMapper;
    @Mock
    private ClientEntityMapper clientEntityMapper;
    @Mock
    private FoodOrderingAppUserEntity foodOrderingAppUserEntity;
    @Mock
    private FoodOrderingAppUserJpaRepository foodOrderingAppUserJpaRepository;
    @Mock
    private FoodOrderingAppUserEntityMapper foodOrderingAppUserEntityMapper;
    @InjectMocks
    private ClientRepository clientRepository;

    private ClientEntity clientEntity;
    private Client client;

    private Set<FoodOrderEntity> foodOrderEntities;
    private FoodOrderEntity foodOrderEntity;
    private FoodOrder foodOrder;

    @BeforeEach
    void setUp() {
        clientEntity = new ClientEntity();
        clientEntity.setId(1L);
        clientEntity.setFullName("John Doe");
        clientEntity.setPhoneNumber("1234567890");

        client = Client.builder()
                .clientId(1L)
                .fullName("John Doe")
                .phoneNumber("1234567890")
                .build();

        foodOrderEntity = new FoodOrderEntity();
        foodOrderEntity.setId(1L);


        foodOrderEntities = new HashSet<>();
        foodOrderEntities.add(foodOrderEntity);

        foodOrder = FoodOrder.builder()
                .foodOrderId(1L)
                .orderTime(LocalDateTime.now())
                .foodOrderStatus(FoodOrderStatus.CONFIRMED.toString())
                .totalPrice(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void findByIdShouldReturnClient() {
        when(clientJpaRepository.findByUserId(1L)).thenReturn(Optional.of(clientEntity));
        when(clientEntityMapper.mapFromEntity(clientEntity)).thenReturn(client);

        Client foundClient = clientRepository.findByUserId(1L);

        assertThat(foundClient).isNotNull();
        assertThat(foundClient.getClientId()).isEqualTo(client.getClientId());
        assertThat(foundClient.getFullName()).isEqualTo(client.getFullName());
        assertThat(foundClient.getPhoneNumber()).isEqualTo(client.getPhoneNumber());

        verify(clientJpaRepository, times(1)).findByUserId(1L);
        verify(clientEntityMapper, times(1)).mapFromEntity(clientEntity);
    }

    @Test
    void saveShouldPersistClient() {

        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username("testUsername")
                .password("testPassword")
                .email("testEmail")
                .enabled(true)
                .role(Roles.CLIENT.toString())
                .build();
        // Arrange
        Client client = Client.builder()
                .clientId(null)
                .fullName("John Doe")
                .phoneNumber("999888777")
                .user(user)
                .build();

        FoodOrderingAppUserEntity userEntity = new FoodOrderingAppUserEntity();
        userEntity.setUsername(user.getUsername());
        ClientEntity clientEntity = new ClientEntity();
        clientEntity.setFullName(client.getFullName());
        clientEntity.setPhoneNumber(client.getPhoneNumber());
        clientEntity.setUser(userEntity);

        when(foodOrderingAppUserJpaRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(clientJpaRepository.findByUsername(user.getUsername())).thenReturn(Optional.empty());
        when(foodOrderingAppUserEntityMapper.mapToEntity(any(FoodOrderingAppUser.class))).thenReturn(userEntity);
        when(foodOrderingAppUserJpaRepository.save(userEntity)).thenReturn(userEntity);
        when(clientEntityMapper.mapToEntity(any(Client.class))).thenReturn(clientEntity);
        when(clientJpaRepository.save(clientEntity)).thenReturn(clientEntity);
        when(clientEntityMapper.mapFromEntity(clientEntity)).thenReturn(client);

        // Act
        Client savedClient = clientRepository.save(client);

        // Assert
        assertNotNull(savedClient);
        assertEquals(client.getFullName(), savedClient.getFullName());
        assertEquals(client.getPhoneNumber(), savedClient.getPhoneNumber());
        verify(foodOrderingAppUserJpaRepository).findByUsername(user.getUsername());
        verify(clientJpaRepository).findByUsername(user.getUsername());
        verify(clientJpaRepository).save(clientEntity);
        verify(clientEntityMapper).mapToEntity(client);
        verify(clientEntityMapper).mapFromEntity(clientEntity);
        verify(foodOrderingAppUserJpaRepository).save(userEntity);
    }


    @Test
    void findAllShouldReturnAllClients() {
        List<ClientEntity> clientEntities = Arrays.asList(clientEntity);
        when(clientJpaRepository.findAll()).thenReturn(clientEntities);
        when(clientEntityMapper.mapFromEntity(any(ClientEntity.class))).thenReturn(client);

        List<Client> clients = clientRepository.findAll();

        assertThat(clients).isNotNull().hasSize(1);
        Client retrievedClient = clients.get(0);
        assertThat(retrievedClient.getClientId()).isEqualTo(client.getClientId());
        assertThat(retrievedClient.getFullName()).isEqualTo(client.getFullName());
        assertThat(retrievedClient.getPhoneNumber()).isEqualTo(client.getPhoneNumber());

        verify(clientJpaRepository, times(1)).findAll();
        verify(clientEntityMapper, times(1)).mapFromEntity(any(ClientEntity.class));
    }

    @Test
    void deleteByIdShouldRemoveClient() {
        doNothing().when(clientJpaRepository).deleteById(anyLong());

        clientRepository.deleteById(1L);

        verify(clientJpaRepository, times(1)).deleteById(1L);
    }

    @Test
    void deactivateAccountShouldDisableUser() {
        when(foodOrderingAppUserJpaRepository.findById(anyLong())).thenReturn(Optional.of(foodOrderingAppUserEntity));
        doAnswer(invocation -> {
            FoodOrderingAppUserEntity entity = invocation.getArgument(0);
            assertThat(entity.getEnabled()).isFalse();
            return null;
        }).when(foodOrderingAppUserJpaRepository).save(any(FoodOrderingAppUserEntity.class));

        clientRepository.deactivateAccount(1L);

        verify(foodOrderingAppUserJpaRepository, times(1)).findById(anyLong());
        verify(foodOrderingAppUserJpaRepository, times(1)).save(any(FoodOrderingAppUserEntity.class));
    }
    @Test
    void findOrdersByClientIdShouldReturnListOfOrders() {
        ClientEntity mockClientEntity = new ClientEntity();
        mockClientEntity.setId(1L);
        mockClientEntity.setFoodOrderEntities(foodOrderEntities);

        when(clientJpaRepository.findByUserId(1L)).thenReturn(Optional.of(mockClientEntity));
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        List<FoodOrder> orders = clientRepository.findOrdersByClientId(1L);

        assertThat(orders).isNotNull().hasSize(1);
        FoodOrder retrievedOrder = orders.get(0);
        assertThat(retrievedOrder.getFoodOrderId()).isEqualTo(foodOrder.getFoodOrderId());
        assertThat(retrievedOrder.getTotalPrice()).isEqualTo(foodOrder.getTotalPrice());
        assertThat(retrievedOrder.getOrderTime()).isEqualTo(foodOrder.getOrderTime());
        assertThat(retrievedOrder.getFoodOrderStatus()).isEqualTo(foodOrder.getFoodOrderStatus());
    }


}