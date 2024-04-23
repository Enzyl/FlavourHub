package pl.dlusk.infrastructure.database.repository;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.domain.*;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.infrastructure.database.entity.*;
import pl.dlusk.infrastructure.database.repository.jpa.*;
import pl.dlusk.infrastructure.database.repository.mapper.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
@Slf4j
@ExtendWith(MockitoExtension.class)
public class FoodOrderRepositoryTest {
    @Mock private FoodOrderJpaRepository foodOrderJpaRepository;
    @Mock private FoodOrderEntityMapper foodOrderEntityMapper;
    @Mock private ReviewJpaRepository reviewJpaRepository;
    @Mock private ReviewEntityMapper reviewEntityMapper;
    @Mock private ClientEntityMapper clientEntityMapper;
    @Mock private RestaurantEntityMapper restaurantEntityMapper;
    @Mock private DeliveryJpaRepository deliveryJpaRepository;
    @Mock private DeliveryEntityMapper deliveryEntityMapper;
    @Mock private PaymentEntityMapper paymentEntityMapper;
    @Mock private PaymentJpaRepository paymentJpaRepository;
    @Mock private OrderItemEntityMapper orderItemEntityMapper;
    @Mock private OrderItemsJpaRepository orderItemsJpaRepository;
    @Mock private MenuItemEntityMapper menuItemEntityMapper;


    @InjectMocks
    private FoodOrderRepository foodOrderRepository;

    private FoodOrder foodOrder;
    private FoodOrderEntity foodOrderEntity;
    private OrderItem orderItem;
    private Client client;
    private Restaurant restaurant;
    private Delivery delivery;
    private Payment payment;

    @BeforeEach
    void setUp() {

        MenuItem menuItem = MenuItem.builder()
                .menuItemId(1L)
                .name("Pizza Margherita")
                .description("Klasyczna pizza z sosem pomidorowym i mozzarellą")
                .category("Pizza")
                .price(new BigDecimal("24.00"))
                .imagePath("/images/pizza_margherita.jpg")
                .menu(null)
                .build();


        orderItem = OrderItem.builder()
                .orderItemId(1L)
                .menuItem(menuItem)
                .quantity(2)
                .foodOrder(null)
                .build();


        Set<OrderItem> orderItems = Set.of(orderItem);

        client = Mockito.mock(Client.class);
        restaurant = Mockito.mock(Restaurant.class);
        delivery = Mockito.mock(Delivery.class);
        payment = Mockito.mock(Payment.class);


        foodOrder = FoodOrder.builder()
                .foodOrderId(1L)
                .orderTime(LocalDateTime.now())
                .foodOrderStatus(FoodOrderStatus.CONFIRMED.toString())
                .totalPrice(new BigDecimal("100.00"))
                .client(client)
                .restaurant(restaurant)
                .orderItems(Set.of(orderItem))
                .delivery(delivery)
                .payment(payment)
                .build();


        orderItems.forEach(item -> item.withFoodOrder(foodOrder));


        foodOrderEntity = new FoodOrderEntity();
        foodOrderEntity.setId(foodOrder.getFoodOrderId());
        foodOrderEntity.setStatus(foodOrder.getFoodOrderStatus());
        foodOrderEntity.setTotalPrice(foodOrder.getTotalPrice());
        foodOrderEntity.setOrderTime(foodOrder.getOrderTime());
        foodOrderEntity.setClientEntity(new ClientEntity());
        foodOrderEntity.setRestaurantEntity(new RestaurantEntity());


    }


    @Test
    void saveShouldPersistFoodOrderWithAllAssociations() {

        FoodOrderEntity foodOrderEntity = new FoodOrderEntity();
        DeliveryEntity deliveryEntity = new DeliveryEntity();
        PaymentEntity paymentEntity = new PaymentEntity();
        OrderItemEntity orderItemEntity = new OrderItemEntity();

        when(foodOrderEntityMapper.mapToEntity(foodOrder)).thenReturn(foodOrderEntity);
        when(foodOrderJpaRepository.save(foodOrderEntity)).thenReturn(foodOrderEntity);
        when(foodOrderEntityMapper.mapFromEntity(foodOrderEntity)).thenReturn(foodOrder);

        when(clientEntityMapper.mapToEntity(client)).thenReturn(new ClientEntity());
        when(restaurantEntityMapper.mapToEntity(restaurant)).thenReturn(new RestaurantEntity());

        when(deliveryEntityMapper.mapToEntity(delivery)).thenReturn(deliveryEntity);
        when(paymentEntityMapper.mapToEntity(payment)).thenReturn(paymentEntity);

        when(orderItemEntityMapper.mapToEntity(orderItem)).thenReturn(orderItemEntity);
        when(menuItemEntityMapper.mapToEntity(orderItem.getMenuItem())).thenReturn(new MenuItemEntity());


        FoodOrder result = foodOrderRepository.save(foodOrder);


        verify(foodOrderEntityMapper).mapToEntity(foodOrder);
        verify(foodOrderJpaRepository).save(foodOrderEntity);
        verify(foodOrderEntityMapper).mapFromEntity(foodOrderEntity);

        verify(clientEntityMapper).mapToEntity(client);
        verify(restaurantEntityMapper).mapToEntity(restaurant);

        verify(deliveryEntityMapper).mapToEntity(delivery);
        verify(deliveryJpaRepository).save(deliveryEntity);
        verify(paymentEntityMapper).mapToEntity(payment);
        verify(paymentJpaRepository).save(paymentEntity);

        verify(orderItemEntityMapper).mapToEntity(orderItem);
        verify(menuItemEntityMapper).mapToEntity(orderItem.getMenuItem());
        verify(orderItemsJpaRepository).saveAll(any(Set.class));

        assertThat(result).isEqualTo(foodOrder);
    }

    @Test
    void findByIdShouldReturnFoodOrder() {
        when(foodOrderJpaRepository.findById(anyLong())).thenReturn(Optional.of(foodOrderEntity));
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);


        Optional<FoodOrder> foundFoodOrder = foodOrderRepository.findById(1L);


        assertThat(foundFoodOrder).isPresent();
        FoodOrder foodOrderResult = foundFoodOrder.get();
        assertThat(foodOrderResult.getFoodOrderId()).isEqualTo(foodOrder.getFoodOrderId());
        assertThat(foodOrderResult.getOrderTime()).isEqualTo(foodOrder.getOrderTime());
        assertThat(foodOrderResult.getFoodOrderStatus()).isEqualTo(foodOrder.getFoodOrderStatus());
        assertThat(foodOrderResult.getTotalPrice()).isEqualTo(foodOrder.getTotalPrice());

    }

    @Test
    void findAllShouldReturnAllFoodOrders() {
        List<FoodOrderEntity> foodOrderEntities = List.of(foodOrderEntity);
        when(foodOrderJpaRepository.findAll()).thenReturn(foodOrderEntities);
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);


        List<FoodOrder> allFoodOrders = foodOrderRepository.findAll();


        assertThat(allFoodOrders).hasSize(1);
        FoodOrder foodOrderResult = allFoodOrders.get(0);
        assertThat(foodOrderResult.getFoodOrderId()).isEqualTo(foodOrder.getFoodOrderId());
        assertThat(foodOrderResult.getOrderTime()).isEqualTo(foodOrder.getOrderTime());
        assertThat(foodOrderResult.getFoodOrderStatus()).isEqualTo(foodOrder.getFoodOrderStatus());
        assertThat(foodOrderResult.getTotalPrice()).isEqualTo(foodOrder.getTotalPrice());

    }

    @Test
    void findByClientIdShouldReturnListOfOrders() {
        List<FoodOrderEntity> foodOrderEntities = List.of(foodOrderEntity);
        when(foodOrderJpaRepository.findByClientEntityId(anyLong())).thenReturn(foodOrderEntities);
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        List<FoodOrder> result = foodOrderRepository.findByClientId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(foodOrder);
    }

    @Test
    void findByRestaurantIdShouldReturnListOfOrders() {
        List<FoodOrderEntity> foodOrderEntities = List.of(foodOrderEntity);
        when(foodOrderJpaRepository.findByRestaurantEntityId(anyLong())).thenReturn(foodOrderEntities);
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        List<FoodOrder> result = foodOrderRepository.findByRestaurantId(1L);

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(foodOrder);
    }

    @Test
    void deleteByIdShouldRemoveOrder() {
        when(foodOrderJpaRepository.existsById(anyLong())).thenReturn(true);
        doNothing().when(foodOrderJpaRepository).deleteById(anyLong());

        foodOrderRepository.deleteById(1L);

        verify(foodOrderJpaRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteByIdShouldThrowExceptionWhenOrderNotFound() {
        when(foodOrderJpaRepository.existsById(anyLong())).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> foodOrderRepository.deleteById(1L));

        verify(foodOrderJpaRepository, never()).deleteById(anyLong());
    }

    @Test
    void findByOrderStatusShouldReturnListOfOrders() {
        List<FoodOrderEntity> foodOrderEntities = List.of(foodOrderEntity);
        when(foodOrderJpaRepository.findByStatus(anyString())).thenReturn(foodOrderEntities);
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        List<FoodOrder> result = foodOrderRepository.findByOrderStatus("Przygotowany");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualTo(foodOrder);
    }
    @Test
    void findByDateRangeShouldReturnOrdersWithinRange() {
        LocalDateTime start = LocalDateTime.of(2022, 3, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2022, 3, 31, 23, 59);
        List<FoodOrderEntity> foodOrderEntities = List.of(foodOrderEntity);

        when(foodOrderJpaRepository.findByOrderTimeBetween(start, end)).thenReturn(foodOrderEntities);
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        List<FoodOrder> result = foodOrderRepository.findByDateRange(start, end);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualToComparingFieldByField(foodOrder);
    }
    @Test
    void addReviewShouldAddReviewToOrder() {
        Long orderId = 1L;
        Review review = Review.builder()
                .reviewId(1L)
                .rating(5)
                .comment("Excellent service")
                .reviewDate(LocalDateTime.now())
                .build();

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(review.getReviewId());
        reviewEntity.setRating(review.getRating());
        reviewEntity.setComment(review.getComment());
        reviewEntity.setReviewDate(review.getReviewDate());
        reviewEntity.setFoodOrderEntity(foodOrderEntity);

        when(foodOrderJpaRepository.findById(orderId)).thenReturn(Optional.of(foodOrderEntity));
        when(reviewEntityMapper.mapToEntity(review)).thenReturn(reviewEntity);
        when(reviewJpaRepository.save(reviewEntity)).thenReturn(reviewEntity);
        when(reviewEntityMapper.mapFromEntity(reviewEntity)).thenReturn(review);

        Review savedReview = foodOrderRepository.addReview(orderId, review);

        assertThat(savedReview).isNotNull();
        assertThat(savedReview).isEqualToComparingFieldByField(review);
        verify(foodOrderJpaRepository, times(1)).findById(orderId);
        verify(reviewEntityMapper, times(1)).mapToEntity(review);
        verify(reviewJpaRepository, times(1)).save(reviewEntity);
        verify(reviewEntityMapper, times(1)).mapFromEntity(reviewEntity);
    }

}