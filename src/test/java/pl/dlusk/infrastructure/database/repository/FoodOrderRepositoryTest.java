package pl.dlusk.infrastructure.database.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.MenuItem;
import pl.dlusk.domain.OrderItem;
import pl.dlusk.domain.Review;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
import pl.dlusk.infrastructure.database.entity.ReviewEntity;
import pl.dlusk.infrastructure.database.repository.jpa.FoodOrderJpaRepository;
import pl.dlusk.infrastructure.database.repository.jpa.ReviewJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.FoodOrderEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.ReviewEntityMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FoodOrderRepositoryTest {
    @Mock
    private FoodOrderJpaRepository foodOrderJpaRepository;

    @Mock
    private FoodOrderEntityMapper foodOrderEntityMapper;
    @Mock
    private ReviewEntityMapper reviewEntityMapper;

    @Mock
    private ReviewJpaRepository reviewJpaRepository;

    @InjectMocks
    private FoodOrderRepository foodOrderRepository;

    private FoodOrder foodOrder;
    private FoodOrderEntity foodOrderEntity;
    private OrderItem orderItem;

    @BeforeEach
    void setUp() {
        // Inicjalizacja MenuItem
        MenuItem menuItem = MenuItem.builder()
                .menuItemId(1L)
                .name("Pizza Margherita")
                .description("Klasyczna pizza z sosem pomidorowym i mozzarellą")
                .category("Pizza")
                .price(new BigDecimal("24.00"))
                .imagePath("/images/pizza_margherita.jpg")
                .menu(null) // Jeśli istnieje odpowiedni obiekt Menu, należy go tu przypisać
                .build();

        // Inicjalizacja OrderItem
        orderItem = OrderItem.builder()
                .orderItemId(1L)
                .menuItem(menuItem)
                .quantity(2)
                .foodOrder(null) // To zostanie uzupełnione, gdy FoodOrder zostanie stworzony
                .build();

        // Ustawienie Set<OrderItem> dla FoodOrder
        Set<OrderItem> orderItems = Set.of(orderItem);

        // Inicjalizacja FoodOrder
        foodOrder = FoodOrder.builder()
                .foodOrderId(1L)
                .orderTime(LocalDateTime.now())
                .foodOrderStatus("Przygotowany")
                .totalPrice(new BigDecimal("48.00")) // Przykładowa wartość, może wymagać dostosowania
                .client(null) // Przypisz obiekt Client, jeśli dostępny
                .restaurant(null) // Przypisz obiekt Restaurant, jeśli dostępny
                .orderItems(orderItems)
                .review(null) // Przypisz obiekt Review, jeśli dostępny
                .delivery(null) // Przypisz obiekt Delivery, jeśli dostępny
                .payment(null) // Przypisz obiekt Payment, jeśli dostępny
                .build();

        // Ustawienie powiązania zwrotnego z OrderItem do FoodOrder
        orderItems.forEach(item -> item.withFoodOrder(foodOrder));

        // Inicjalizacja FoodOrderEntity
        foodOrderEntity = new FoodOrderEntity();
        foodOrderEntity.setId(foodOrder.getFoodOrderId());
        foodOrderEntity.setStatus(foodOrder.getFoodOrderStatus());
        foodOrderEntity.setTotalPrice(foodOrder.getTotalPrice());
        foodOrderEntity.setOrderTime(foodOrder.getOrderTime());
        // Uzupełnij pozostałe pola encji FoodOrderEntity, jeśli to konieczne
    }


    @Test
    void saveShouldPersistFoodOrder() {
        // Przygotowanie danych wejściowych
        foodOrder = FoodOrder.builder()
                .foodOrderId(1L)
                .orderTime(LocalDateTime.now())
                .foodOrderStatus("Przygotowany")
                .totalPrice(new BigDecimal("100.00"))
                // Uzupełnij pozostałe pola według potrzeb
                .build();

        when(foodOrderEntityMapper.mapToEntity(any(FoodOrder.class))).thenReturn(foodOrderEntity);
        when(foodOrderJpaRepository.save(any(FoodOrderEntity.class))).thenReturn(foodOrderEntity);
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        // Akcja
        FoodOrder savedFoodOrder = foodOrderRepository.save(foodOrder);

        // Weryfikacja
        assertThat(savedFoodOrder).isNotNull();
        assertThat(savedFoodOrder.getFoodOrderId()).isEqualTo(foodOrder.getFoodOrderId());
        assertThat(savedFoodOrder.getOrderTime()).isEqualTo(foodOrder.getOrderTime());
        assertThat(savedFoodOrder.getFoodOrderStatus()).isEqualTo(foodOrder.getFoodOrderStatus());
        assertThat(savedFoodOrder.getTotalPrice()).isEqualTo(foodOrder.getTotalPrice());
        // Weryfikuj pozostałe pola
    }

    @Test
    void findByIdShouldReturnFoodOrder() {
        when(foodOrderJpaRepository.findById(anyLong())).thenReturn(Optional.of(foodOrderEntity));
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        // Akcja
        Optional<FoodOrder> foundFoodOrder = foodOrderRepository.findById(1L);

        // Weryfikacja
        assertThat(foundFoodOrder).isPresent();
        FoodOrder foodOrderResult = foundFoodOrder.get();
        assertThat(foodOrderResult.getFoodOrderId()).isEqualTo(foodOrder.getFoodOrderId());
        assertThat(foodOrderResult.getOrderTime()).isEqualTo(foodOrder.getOrderTime());
        assertThat(foodOrderResult.getFoodOrderStatus()).isEqualTo(foodOrder.getFoodOrderStatus());
        assertThat(foodOrderResult.getTotalPrice()).isEqualTo(foodOrder.getTotalPrice());
        // Weryfikuj pozostałe pola
    }

    @Test
    void findAllShouldReturnAllFoodOrders() {
        List<FoodOrderEntity> foodOrderEntities = List.of(foodOrderEntity);
        when(foodOrderJpaRepository.findAll()).thenReturn(foodOrderEntities);
        when(foodOrderEntityMapper.mapFromEntity(any(FoodOrderEntity.class))).thenReturn(foodOrder);

        // Akcja
        List<FoodOrder> allFoodOrders = foodOrderRepository.findAll();

        // Weryfikacja
        assertThat(allFoodOrders).hasSize(1);
        FoodOrder foodOrderResult = allFoodOrders.get(0);
        assertThat(foodOrderResult.getFoodOrderId()).isEqualTo(foodOrder.getFoodOrderId());
        assertThat(foodOrderResult.getOrderTime()).isEqualTo(foodOrder.getOrderTime());
        assertThat(foodOrderResult.getFoodOrderStatus()).isEqualTo(foodOrder.getFoodOrderStatus());
        assertThat(foodOrderResult.getTotalPrice()).isEqualTo(foodOrder.getTotalPrice());
        // Weryfikuj pozostałe pola
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