package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.RestaurantDeliveryArea;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryAreaEntity;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryStreetEntity;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;

import static org.assertj.core.api.Assertions.assertThat;
public class RestaurantDeliveryAreaEntityMapperTest {
    private RestaurantDeliveryAreaEntityMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(RestaurantDeliveryAreaEntityMapper.class);
    }

    @Test
    void shouldMapFromEntity() {
        // Given
        RestaurantEntity restaurantEntity = new RestaurantEntity();
        restaurantEntity.setId(1L);

        RestaurantDeliveryStreetEntity deliveryStreetEntity = new RestaurantDeliveryStreetEntity();
        deliveryStreetEntity.setId(2L);

        RestaurantDeliveryAreaEntity entity = new RestaurantDeliveryAreaEntity();
        entity.setId(3L);
        entity.setRestaurantEntity(restaurantEntity);
        entity.setDeliveryStreet(deliveryStreetEntity);

        // When
        RestaurantDeliveryArea result = mapper.mapFromEntity(entity);

        // Then
        assertThat(result.getRestaurantDeliveryAreaId()).isEqualTo(entity.getId());
        assertThat(result.getRestaurant()).isNotNull();
        assertThat(result.getRestaurant().getRestaurantId()).isEqualTo(restaurantEntity.getId());
        assertThat(result.getDeliveryStreet()).isNotNull();
        assertThat(result.getDeliveryStreet().getRestaurantDeliveryStreetId()).isEqualTo(deliveryStreetEntity.getId());
    }
}