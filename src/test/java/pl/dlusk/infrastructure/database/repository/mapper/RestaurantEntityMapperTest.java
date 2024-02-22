package pl.dlusk.infrastructure.database.repository.mapper;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class RestaurantEntityMapperTest {
    @Test
    void shouldMapRestaurantEntityToRestaurant() {
        // Given
        RestaurantEntity restaurantEntity = new RestaurantEntity();
        restaurantEntity.setId(1L);
        restaurantEntity.setName("Test Restaurant");
        restaurantEntity.setDescription("Test Description");
        restaurantEntity.setImagePath("path/to/image");

        // When
        Restaurant restaurant = Mappers.getMapper(RestaurantEntityMapper.class).mapFromEntity(restaurantEntity);

        // Then
        assertThat(restaurant.getRestaurantId()).isEqualTo(restaurantEntity.getId());
        assertThat(restaurant.getName()).isEqualTo(restaurantEntity.getName());
        assertThat(restaurant.getDescription()).isEqualTo(restaurantEntity.getDescription());
        assertThat(restaurant.getImagePath()).isEqualTo(restaurantEntity.getImagePath());
        // Ignored properties should not be mapped
        assertThat(restaurant.getAddress()).isNull();
        assertThat(restaurant.getOwner()).isNull();
    }
    @Test
    void shouldMapRestaurantToRestaurantEntity() {
        // Given
        Restaurant restaurant = Restaurant.builder()
                .restaurantId(1L)
                .name("Test Restaurant")
                .description("Test Description")
                .imagePath("path/to/image")
                .build();

        // When
        RestaurantEntity restaurantEntity = Mappers.getMapper(RestaurantEntityMapper.class).mapToEntity(restaurant);

        // Then
        assertThat(restaurantEntity.getId()).isEqualTo(restaurant.getRestaurantId());
        assertThat(restaurantEntity.getName()).isEqualTo(restaurant.getName());
        assertThat(restaurantEntity.getDescription()).isEqualTo(restaurant.getDescription());
        assertThat(restaurantEntity.getImagePath()).isEqualTo(restaurant.getImagePath());
        // Ignored properties should not be mapped
        assertThat(restaurantEntity.getAddress()).isNull();
        assertThat(restaurantEntity.getOwnerEntity()).isNull();
    }

}