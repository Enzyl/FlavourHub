package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.RestaurantDeliveryStreet;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryStreetEntity;

import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
public class RestaurantDeliveryStreetEntityMapperTest {
    private RestaurantDeliveryStreetEntityMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(RestaurantDeliveryStreetEntityMapper.class);
    }

    @Test
    void shouldMapFromEntity() {
        // Given
        RestaurantDeliveryStreetEntity entity = new RestaurantDeliveryStreetEntity();
        entity.setId(1L);
        entity.setStreetName("Main Street");
        entity.setPostalCode("12345");
        entity.setDistrict("Downtown");

        // When
        RestaurantDeliveryStreet street = mapper.mapFromEntity(entity);

        // Then
        assertThat(street).isNotNull();
        assertThat(street.getRestaurantDeliveryStreetId()).isEqualTo(entity.getId());
        assertThat(street.getStreetName()).isEqualTo(entity.getStreetName());
        assertThat(street.getPostalCode()).isEqualTo(entity.getPostalCode());
        assertThat(street.getDistrict()).isEqualTo(entity.getDistrict());
        assertThat(street.getDeliveryAreas()).isNull(); // Ignored in mapping
    }

    @Test
    void shouldMapToEntity() {
        // Given
        RestaurantDeliveryStreet street = RestaurantDeliveryStreet.builder()
                .restaurantDeliveryStreetId(1L)
                .streetName("Main Street")
                .postalCode("12345")
                .district("Downtown")
                .deliveryAreas(new HashSet<>()) // Empty set for simplicity
                .build();

        // When
        RestaurantDeliveryStreetEntity entity = mapper.mapToEntity(street);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(street.getRestaurantDeliveryStreetId());
        assertThat(entity.getStreetName()).isEqualTo(street.getStreetName());
        assertThat(entity.getPostalCode()).isEqualTo(street.getPostalCode());
        assertThat(entity.getDistrict()).isEqualTo(street.getDistrict());
        assertThat(entity.getDeliveryAreas()).isNull(); // Ignored in mapping
    }
}