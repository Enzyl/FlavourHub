package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.dlusk.domain.MenuItem;
import pl.dlusk.infrastructure.database.entity.MenuItemEntity;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
public class MenuItemEntityMapperTest {

    private final MenuItemEntityMapper mapper = Mappers.getMapper(MenuItemEntityMapper.class);

    @Test
    void shouldMapMenuItemToMenuItemEntity() {
        // Given
        MenuItem menuItem = MenuItem.builder()
                .menuItemId(1L)
                .name("Pizza")
                .description("Delicious Italian pizza")
                .category("Italian")
                .price(new BigDecimal("15.00"))
                .imagePath("path/to/image")
                .build();

        // When
        MenuItemEntity entity = mapper.mapToEntity(menuItem);

        // Then
        assertThat(entity.getId()).isEqualTo(menuItem.getMenuItemId());
        assertThat(entity.getName()).isEqualTo(menuItem.getName());
        assertThat(entity.getDescription()).isEqualTo(menuItem.getDescription());
        assertThat(entity.getCategory()).isEqualTo(menuItem.getCategory());
        assertThat(entity.getPrice()).isEqualByComparingTo(menuItem.getPrice());
        assertThat(entity.getImagePath()).isEqualTo(menuItem.getImagePath());
    }

    @Test
    void shouldMapMenuItemEntityToMenuItem() {
        // Given
        MenuItemEntity entity = new MenuItemEntity();
        entity.setId(1L);
        entity.setName("Pizza");
        entity.setDescription("Delicious Italian pizza");
        entity.setCategory("Italian");
        entity.setPrice(new BigDecimal("15.00"));
        entity.setImagePath("path/to/image");

        // When
        MenuItem menuItem = mapper.mapFromEntity(entity);

        // Then
        assertThat(menuItem.getMenuItemId()).isEqualTo(entity.getId());
        assertThat(menuItem.getName()).isEqualTo(entity.getName());
        assertThat(menuItem.getDescription()).isEqualTo(entity.getDescription());
        assertThat(menuItem.getCategory()).isEqualTo(entity.getCategory());
        assertThat(menuItem.getPrice()).isEqualByComparingTo(entity.getPrice());
        assertThat(menuItem.getImagePath()).isEqualTo(entity.getImagePath());
    }
}
