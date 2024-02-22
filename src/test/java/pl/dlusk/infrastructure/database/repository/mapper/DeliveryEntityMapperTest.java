package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.Delivery;
import pl.dlusk.infrastructure.database.entity.DeliveryEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
public class DeliveryEntityMapperTest {
    private DeliveryEntityMapper mapper = Mappers.getMapper(DeliveryEntityMapper.class);

    @Test
    void shouldMapDeliveryEntityToDelivery() {
        // Arrange
        DeliveryEntity entity = new DeliveryEntity();
        entity.setId(1L);
        entity.setDeliveryAddress("123 Main St");
        entity.setDeliveryTime(LocalDateTime.now());
        entity.setDeliveryStatus("Delivered");

        // Act
        Delivery delivery = mapper.mapFromEntity(entity);

        // Assert
        assertThat(delivery).isNotNull();
        assertThat(delivery.getDeliveryAddress()).isEqualTo(entity.getDeliveryAddress());
        assertThat(delivery.getDeliveryTime()).isEqualTo(entity.getDeliveryTime());
        assertThat(delivery.getDeliveryStatus()).isEqualTo(entity.getDeliveryStatus());
        // 'foodOrder' is ignored, so it should be null
        assertThat(delivery.getFoodOrder()).isNull();
    }

    @Test
    void shouldMapDeliveryToDeliveryEntity() {
        // Arrange
        Delivery delivery = Delivery.builder()
                .deliveryAddress("123 Main St")
                .deliveryTime(LocalDateTime.now())
                .deliveryStatus("Delivered")
                .build();

        // Act
        DeliveryEntity entity = mapper.mapToEntity(delivery);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getDeliveryAddress()).isEqualTo(delivery.getDeliveryAddress());
        assertThat(entity.getDeliveryTime()).isEqualTo(delivery.getDeliveryTime());
        assertThat(entity.getDeliveryStatus()).isEqualTo(delivery.getDeliveryStatus());
        // 'foodOrderEntity' cannot be checked since it's ignored in mapping
    }
}
