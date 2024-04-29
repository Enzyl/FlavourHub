package pl.dlusk.business;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.ClientOrderHistory;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.OrderItem;
import pl.dlusk.infrastructure.database.repository.ClientRepository;
import pl.dlusk.infrastructure.database.repository.FoodOrderRepository;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    @Mock

    private FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    @Mock
    private FoodOrderDAO foodOrderDAO;

    @Mock
    private FoodOrderService foodOrderService;

    @InjectMocks
    private ClientService clientService;

    @AfterEach
    public void tearDown() {
        Mockito.reset(clientRepository, foodOrderingAppUserRepository, foodOrderDAO, foodOrderService);
    }

    @Test
    public void registerClient_ShouldSaveClient() {
        // Arrange
        Client expectedClient = Client.builder()
                .clientId(1L)
                .fullName("John Doe")
                .build();
        Mockito.when(clientRepository.save(expectedClient)).thenReturn(expectedClient);

        // Act
        Client savedClient = clientService.registerClient(expectedClient);

        // Assert
        assertNotNull(savedClient);
        assertEquals(expectedClient, savedClient);
        verify(clientRepository, times(1)).save(expectedClient);
    }

    // TODO: REPAIR THIS STUPID TEST
    @Test
    public void getClientOrderHistory_WithExistingOrders_ShouldReturnOrderHistory() throws Exception {
        // Arrange
        String username = "testUser";
        Long clientId = 1L;
        List<FoodOrder> foodOrders = Collections.singletonList(Mockito.mock(FoodOrder.class));
        Set<OrderItem> orderItems = Collections.singleton(Mockito.mock(OrderItem.class));
        ClientOrderHistory.FoodOrderRequest expectedRequest =
                ClientOrderHistory.FoodOrderRequest.builder().build();

        Mockito.when(foodOrderingAppUserRepository.findIdByUsername(username)).thenReturn(clientId);
        Mockito.when(clientRepository.findOrdersByClientId(clientId)).thenReturn(foodOrders);
        Mockito.when(foodOrderDAO.findOrderItemsByFoodOrderId(foodOrders.get(0).getFoodOrderId())).thenReturn(orderItems);
        Mockito.when(foodOrderService.convertToFoodOrderRequest(foodOrders.get(0), orderItems)).thenReturn(expectedRequest);

        // Act
        ClientOrderHistory orderHistory = clientService.getClientOrderHistory(username);

        // Assert
        assertNotNull(orderHistory);
        assertEquals(clientId, orderHistory.getCustomerId());
        assertEquals(1, orderHistory.getCustomerFoodOrders().size());
        verify(foodOrderingAppUserRepository, times(1)).findIdByUsername(username);
        verify(clientRepository, times(1)).findOrdersByClientId(clientId);
        verify(foodOrderDAO, times(1)).findOrderItemsByFoodOrderId(foodOrders.get(0).getFoodOrderId());
        verify(foodOrderService).convertToFoodOrderRequest(foodOrders.get(0), orderItems);
        verifyNoMoreInteractions(foodOrderService);
    }

    @Test
    public void getClientOrderHistory_WithNoOrders_ShouldReturnEmptyOrderHistory() throws Exception {
        // Arrange
        String username = "testUser";
        Long clientId = 1L;
        List<FoodOrder> foodOrders = Collections.emptyList();

        Mockito.when(foodOrderingAppUserRepository.findIdByUsername(username)).thenReturn(clientId);

        // Act
        ClientOrderHistory orderHistory = clientService.getClientOrderHistory(username);

        // Assert
        assertNotNull(orderHistory);
        assertEquals(clientId, orderHistory.getCustomerId());
        assertTrue(orderHistory.getCustomerFoodOrders().isEmpty());
        verify(foodOrderingAppUserRepository, times(1)).findIdByUsername(username);
    }


    @Test
    public void getClientByUsername_ExistingUser_ShouldReturnClient() throws Exception {
        // Arrange
        String username = "testUser";
        Long clientId = 1L;
        Client expectedClient = Client.builder().clientId(clientId).build();

        Mockito.when(foodOrderingAppUserRepository.findIdByUsername(username)).thenReturn(clientId);
        Mockito.when(clientRepository.findByUserId(clientId)).thenReturn(expectedClient);

        // Act
        Client retrievedClient = clientService.getClientByUsername(username);

        // Assert
        assertNotNull(retrievedClient);
        assertEquals(expectedClient, retrievedClient);
        verify(foodOrderingAppUserRepository, times(1)).findIdByUsername(username);
        verify(clientRepository, times(1)).findByUserId(clientId);
    }

}