package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.RestaurantAddress;
import pl.dlusk.infrastructure.database.entity.RestaurantAddressEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class RestaurantAddressEntityMapperTest {
    private final RestaurantAddressEntityMapper mapper = Mappers.getMapper(RestaurantAddressEntityMapper.class);

    @Test
    void shouldMapRestaurantAddressEntityToRestaurantAddress() {
        // Given
        RestaurantAddressEntity entity = new RestaurantAddressEntity();
        entity.setId(1L);
        entity.setCity("Test City");
        entity.setPostalCode("12345");
        entity.setAddress("Test Address");

        // When
        RestaurantAddress address = mapper.mapFromEntity(entity);

        // Then
        assertThat(address).isNotNull();
        assertThat(address.getRestaurantAddressId()).isEqualTo(entity.getId());
        assertThat(address.getCity()).isEqualTo(entity.getCity());
        assertThat(address.getPostalCode()).isEqualTo(entity.getPostalCode());
        assertThat(address.getAddress()).isEqualTo(entity.getAddress());
    }

    @Test
    void shouldMapRestaurantAddressToRestaurantAddressEntity() {
        // Given
        RestaurantAddress address = RestaurantAddress.builder()
                .restaurantAddressId(1L)
                .city("Test City")
                .postalCode("12345")
                .address("Test Address")
                .build();

        // When
        RestaurantAddressEntity entity = mapper.mapToEntity(address);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(address.getRestaurantAddressId());
        assertThat(entity.getCity()).isEqualTo(address.getCity());
        assertThat(entity.getPostalCode()).isEqualTo(address.getPostalCode());
        assertThat(entity.getAddress()).isEqualTo(address.getAddress());
    }
}