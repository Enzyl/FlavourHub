package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
import pl.dlusk.infrastructure.database.repository.mapper.FoodOrderEntityMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

public class FoodOrderEntityMapperTest {

    private FoodOrderEntityMapper mapper = Mappers.getMapper(FoodOrderEntityMapper.class);

    @Test
    void shouldMapFoodOrderEntityToFoodOrder() {
        // Arrange
        FoodOrderEntity entity = new FoodOrderEntity();
        entity.setId(1L);
        entity.setOrderTime(LocalDateTime.now());
        entity.setStatus("Delivered");
        entity.setTotalPrice(new BigDecimal("100.00"));

        // Act
        FoodOrder foodOrder = mapper.mapFromEntity(entity);

        // Assert
        assertThat(foodOrder).isNotNull();
        assertThat(foodOrder.getFoodOrderId()).isEqualTo(entity.getId());
        assertThat(foodOrder.getOrderTime()).isEqualTo(entity.getOrderTime());
        assertThat(foodOrder.getFoodOrderStatus()).isEqualTo(entity.getStatus());
        assertThat(foodOrder.getTotalPrice()).isEqualTo(entity.getTotalPrice());
        // Ignored fields should be null
        assertThat(foodOrder.getClient()).isNull();
        assertThat(foodOrder.getRestaurant()).isNull();
        assertThat(foodOrder.getOrderItems()).isNull();
        assertThat(foodOrder.getReview()).isNull();
        assertThat(foodOrder.getDelivery()).isNull();
        assertThat(foodOrder.getPayment()).isNull();
    }

    @Test
    void shouldMapFoodOrderToFoodOrderEntity() {
        // Arrange
        FoodOrder foodOrder = FoodOrder.builder()
                .foodOrderId(1L)
                .orderTime(LocalDateTime.now())
                .foodOrderStatus("Delivered")
                .totalPrice(new BigDecimal("100.00"))
                .build();

        // Act
        FoodOrderEntity entity = mapper.mapToEntity(foodOrder);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(foodOrder.getFoodOrderId());
        assertThat(entity.getOrderTime()).isEqualTo(foodOrder.getOrderTime());
        assertThat(entity.getStatus()).isEqualTo(foodOrder.getFoodOrderStatus());
        assertThat(entity.getTotalPrice()).isEqualTo(foodOrder.getTotalPrice());
        // Ignored fields cannot be checked as they are not mapped
    }
}
