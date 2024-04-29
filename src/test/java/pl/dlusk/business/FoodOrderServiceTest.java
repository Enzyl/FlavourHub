package pl.dlusk.business;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.api.controller.ClientController;
import pl.dlusk.business.dao.*;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.ClientOrderHistory;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.OrderItem;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.infrastructure.database.repository.ClientRepository;
import pl.dlusk.infrastructure.database.repository.FoodOrderRepository;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.domain.*;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserDAO;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
@ExtendWith(MockitoExtension.class)
class FoodOrderServiceTest {
    @Mock
    private HttpSession session;
    @Mock
    private FoodOrderDAO foodOrderDAO;
    @Mock
    private PaymentDAO paymentDAO;

    @Mock
    private ClientController clientController;
    @Mock
    private FoodOrderingAppUserDAO foodOrderingAppUserDAO;
    @Mock
    private ClientDAO clientDAO;
    @Mock
    private RestaurantDAO restaurantDAO;
    @Mock
    private FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    @Mock
    private UtilService utilService;
    @Mock
    private ShoppingCart mockShoppingCart;
    @Mock
    private FoodOrderingAppUser mockUser;
    @Mock
    private Delivery mockDelivery;
    @Mock
    private Payment mockPayment;
    @InjectMocks
    private FoodOrderService foodOrderService;

    @Test
    public void getFoodOrderById_WhenOrderExists_ShouldReturnOrder() {
        // Arrange
        Long orderId = 1L;
        FoodOrder expectedOrder = FoodOrder.builder().build();
        expectedOrder.withFoodOrderId(orderId);
        when(foodOrderDAO.findById(orderId)).thenReturn(Optional.of(expectedOrder));

        // Act
        FoodOrder actualOrder = foodOrderService.getFoodOrderById(orderId);

        // Assert
        assertNotNull(actualOrder, "The order should not be null");
        assertEquals(expectedOrder, actualOrder, "The retrieved order should match the expected order");
        verify(foodOrderDAO).findById(orderId);
    }

    @Test
    public void getFoodOrderById_WhenOrderDoesNotExist_ShouldThrowResourceNotFoundException() {
        // Arrange
        Long orderId = 1L;
        when(foodOrderDAO.findById(orderId)).thenReturn(Optional.empty());

        // Act and Assert
        assertThrows(ResourceNotFoundException.class, () -> foodOrderService.getFoodOrderById(orderId),
                "Expected ResourceNotFoundException to be thrown when order is not found");
        verify(foodOrderDAO).findById(orderId);
    }

    @Test
    public void createFoodOrder_WithValidData_ShouldReturnUniqueOrderNumber() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        String username = "testUser";
        BigDecimal totalValue = BigDecimal.TEN;
        Delivery delivery = Mockito.mock(Delivery.class);
        Payment payment = Mockito.mock(Payment.class);
        ShoppingCart shoppingCart = Mockito.mock(ShoppingCart.class);

        Map<MenuItem, Integer> mockItems = new HashMap<>();
        mockItems.put(Mockito.mock(MenuItem.class), 2);
        Mockito.when(shoppingCart.getItems()).thenReturn(mockItems);

        Long clientId = 10L;
        Client mockClient = Mockito.mock(Client.class);
        Restaurant mockRestaurant = Mockito.mock(Restaurant.class);

        Mockito.when(foodOrderingAppUserRepository.findIdByUsername(username)).thenReturn(clientId);
        Mockito.when(clientDAO.findByUserId(clientId)).thenReturn(mockClient);
        Mockito.when(restaurantDAO.findRestaurantById(restaurantId)).thenReturn(mockRestaurant);


        // Act
        String uniqueOrderNumber = foodOrderService.createFoodOrder(restaurantId, username, totalValue, delivery, payment, shoppingCart);

        // Assert
        assertNotNull(uniqueOrderNumber);
        verify(foodOrderDAO, times(1)).save(any(FoodOrder.class));
    }

    @Test
    public void updateFoodOrderStatus_WithValidData_ShouldUpdateStatus() throws Exception {
        // Arrange
        Long orderId = 1L;
        String newStatus = "DELIVERED";
        // Act
        foodOrderService.updateFoodOrderStatus(orderId, newStatus);
        // Assert
        verify(foodOrderDAO, times(1)).updateFoodOrderStatus(orderId, newStatus);

    }


    @Test
    public void findFoodOrderByOrderNumber_WithExistingOrderNumber_ShouldReturnFoodOrder() throws Exception {
        // Arrange
        String existingOrderNumber = "UNIQUE_ORDER_123";
        Long expectedFoodOrderId = 1L;
        FoodOrder expectedFoodOrder = FoodOrder.builder()
                .foodOrderId(expectedFoodOrderId)
                .build();
        Set<OrderItem> mockOrderItems = new HashSet<>();

        Mockito.when(foodOrderDAO.findFoodOrderByFoodOrderNumber(existingOrderNumber)).thenReturn(expectedFoodOrder);
        Mockito.when(foodOrderDAO.findOrderItemsByFoodOrderId(expectedFoodOrderId)).thenReturn(mockOrderItems);

        // Act
        FoodOrder retrievedFoodOrder = foodOrderService.findFoodOrderByOrderNumber(existingOrderNumber);

        // Assert
        assertNotNull(retrievedFoodOrder);
        assertEquals(expectedFoodOrder, retrievedFoodOrder);
        verify(foodOrderDAO).findFoodOrderByFoodOrderNumber(existingOrderNumber);
        verify(foodOrderDAO).findOrderItemsByFoodOrderId(expectedFoodOrderId);
    }

    @Test
    public void showOrderSummary_ShouldReturnOrderWithItems() {
        // Arrange
        String uniqueOrderNumber = "12345";
        Long foodOrderId = 1L;
        FoodOrder mockOrder = FoodOrder.builder()
                .foodOrderId(foodOrderId)
                .build();

        Set<OrderItem> orderItems = new HashSet<>();
        orderItems.add(OrderItem.builder().build());

        when(foodOrderDAO.findFoodOrderByFoodOrderNumber(uniqueOrderNumber)).thenReturn(mockOrder);
        when(foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderId)).thenReturn(orderItems);

        // Act
        FoodOrder resultOrder = foodOrderService.showOrderSummary(uniqueOrderNumber);

        // Assert
        assertNotNull(resultOrder, "The resulting FoodOrder should not be null");
        assertEquals(mockOrder, resultOrder, "The order should match the initially retrieved order");
        verify(foodOrderDAO).findFoodOrderByFoodOrderNumber(uniqueOrderNumber);
        verify(foodOrderDAO).findOrderItemsByFoodOrderId(foodOrderId);

    }





    @Test
    public void findFoodOrderByOrderNumber_ShouldReturnCorrectFoodOrder() {
        // Arrange
        String orderNumber = "ORD123";
        FoodOrder expectedFoodOrder = FoodOrder.builder().build();
        expectedFoodOrder.withOrderNumber(orderNumber);

        when(foodOrderDAO.findFoodOrderByFoodOrderNumber(orderNumber)).thenReturn(expectedFoodOrder);

        // Act
        FoodOrder actualFoodOrder = foodOrderService.findFoodOrderByOrderNumber(orderNumber);

        // Assert
        assertNotNull(actualFoodOrder, "The food order should not be null");
        assertEquals(expectedFoodOrder, actualFoodOrder, "The returned food order should be the expected one");
        verify(foodOrderDAO).findFoodOrderByFoodOrderNumber(orderNumber);
    }
    @Test
    public void findOrderItemsByFoodOrderId_ShouldReturnOrderItems() {
        // Arrange
        Long foodOrderId = 1L;  // Example Food Order ID
        Set<OrderItem> expectedOrderItems = new HashSet<>();
        expectedOrderItems.add(OrderItem.builder().build());

        when(foodOrderDAO.findOrderItemsByFoodOrderId(foodOrderId)).thenReturn(expectedOrderItems);

        // Act
        Set<OrderItem> actualOrderItems = foodOrderService.findOrderItemsByFoodOrderId(foodOrderId);

        // Assert
        assertNotNull(actualOrderItems, "The returned set of order items should not be null");
        assertEquals(expectedOrderItems, actualOrderItems, "The returned order items should match the expected set");
        verify(foodOrderDAO).findOrderItemsByFoodOrderId(foodOrderId);
    }

    @Test
    public void getFoodOrdersWithStatus_ShouldReturnFilteredEnrichedOrders() throws Exception {
        // Arrange
        Long restaurantId = 1L;
        String expectedStatus = "CONFIRMED";

        List<FoodOrder> mockFoodOrders = new ArrayList<>();
        mockFoodOrders.add(FoodOrder.builder()
                .foodOrderStatus("CONFIRMED")
                .build());
        mockFoodOrders.add(FoodOrder.builder()
                .foodOrderStatus("PENDING")
                .build());
        mockFoodOrders.add(
                FoodOrder.builder()
                .foodOrderStatus("DELIVERED")
                .build()
        );
        Mockito.when(foodOrderDAO.findByRestaurantId(restaurantId)).thenReturn(mockFoodOrders);

        // Act
        List<FoodOrder> filteredOrders = foodOrderService.getFoodOrdersWithStatus(restaurantId, expectedStatus);

        // Assert
        assertNotNull(filteredOrders, "The list of filtered orders should not be null");
        assertEquals(getExpectedNumberOfMatchingOrders(mockFoodOrders, expectedStatus), filteredOrders.size(), "Incorrect number of filtered orders");

        for (FoodOrder order : filteredOrders) {
            assertEquals(expectedStatus, order.getFoodOrderStatus(), "Food order has incorrect status");
        }

        verify(foodOrderDAO).findByRestaurantId(restaurantId);
    }

    private int getExpectedNumberOfMatchingOrders(List<FoodOrder> foodOrders, String expectedStatus) {
        int count = 0;
        for (FoodOrder order : foodOrders) {
            if (expectedStatus.equals(order.getFoodOrderStatus())) {
                count++;
            }
        }
        return count;
    }
    @Test
    public void convertToFoodOrderRequest_ShouldCorrectlyConvert() {
        // Arrange
        Long foodOrderId = 1L;
        FoodOrder mockFoodOrder = FoodOrder.builder().build();
        mockFoodOrder.withFoodOrderId(foodOrderId);
        mockFoodOrder.withOrderTime(LocalDateTime.now());
        mockFoodOrder.withFoodOrderStatus("CONFIRMED");
        mockFoodOrder.withTotalPrice(BigDecimal.valueOf(100.00));

        Restaurant expectedRestaurant = Restaurant.builder().build();
        Payment expectedPayment = Payment.builder().build();
        Set<OrderItem> mockOrderItems = new HashSet<>();
        mockOrderItems.add(OrderItem.builder().build());

        when(restaurantDAO.findRestaurantByFoodOrderId(null)).thenReturn(any());
        when(paymentDAO.findByFoodOrderId(foodOrderId)).thenReturn(expectedPayment);

        // Act
        ClientOrderHistory.FoodOrderRequest result = foodOrderService.convertToFoodOrderRequest(mockFoodOrder, mockOrderItems);

        // Assert
        assertNotNull(result);
        assertEquals(foodOrderId, result.getOrderId());
        assertEquals(mockFoodOrder.getOrderTime(), result.getOrderTime());
        assertEquals(mockFoodOrder.getFoodOrderStatus(), result.getFoodOrderStatus());
        assertEquals(mockFoodOrder.getTotalPrice(), result.getTotalPrice());
        assertSame(expectedRestaurant, result.getRestaurant());
        assertSame(mockOrderItems, result.getOrderItems());
        assertSame(expectedPayment, result.getPayment());
    }



    }

