package pl.dlusk.infrastructure.database.repository.mapper;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.dlusk.domain.OrderItem;
import pl.dlusk.infrastructure.database.entity.OrderItemEntity;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
@ExtendWith(SpringExtension.class)

public class OrderItemEntityMapperTest {

    private final OrderItemEntityMapper mapper = Mappers.getMapper(OrderItemEntityMapper.class);

    @Test
    void shouldMapOrderItemEntityToOrderItem() {
        // Given
        OrderItemEntity entity = new OrderItemEntity();
        entity.setId(1L);
        entity.setQuantity(2);
        // Dodaj tu inne właściwości jeśli są potrzebne

        // When
        OrderItem orderItem = mapper.mapFromEntity(entity);

        // Then
        assertThat(orderItem).isNotNull();
        assertThat(orderItem.getOrderItemId()).isEqualTo(entity.getId());
        assertThat(orderItem.getQuantity()).isEqualTo(entity.getQuantity());
        // Ignored fields are not mapped and should remain null
        assertThat(orderItem.getFoodOrder()).isNull();
        assertThat(orderItem.getMenuItem()).isNull();
    }

    @Test
    void shouldMapOrderItemToOrderItemEntity() {
        // Given
        OrderItem orderItem = OrderItem.builder()
                .orderItemId(1L)
                .quantity(2)
                .build();
        // Dodaj tu inne właściwości jeśli są potrzebne

        // When
        OrderItemEntity entity = mapper.mapToEntity(orderItem);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(orderItem.getOrderItemId());
        assertThat(entity.getQuantity()).isEqualTo(orderItem.getQuantity());
        // Ignored fields are not mapped and should remain null
        assertThat(entity.getFoodOrderEntity()).isNull();
        assertThat(entity.getMenuItemEntity()).isNull();
    }
}