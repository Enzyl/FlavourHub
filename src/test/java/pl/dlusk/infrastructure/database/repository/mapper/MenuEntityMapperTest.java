package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.Menu;
import pl.dlusk.infrastructure.database.entity.MenuEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class MenuEntityMapperTest {

    private final MenuEntityMapper mapper = Mappers.getMapper(MenuEntityMapper.class);

    @Test
    void shouldMapMenuToMenuEntity() {
        // Arrange
        Menu menu = Menu.builder()
                .menuId(1L)
                .name("Italian Cuisine")
                .description("A variety of Italian dishes")
                .build();

        // Act
        MenuEntity entity = mapper.mapToEntity(menu);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(menu.getMenuId());
        assertThat(entity.getName()).isEqualTo(menu.getName());
        assertThat(entity.getDescription()).isEqualTo(menu.getDescription());
        // Ignored fields cannot be checked as they are not mapped
    }

    @Test
    void shouldMapMenuEntityToMenu() {
        // Arrange
        MenuEntity entity = new MenuEntity();
        entity.setId(1L);
        entity.setName("Italian Cuisine");
        entity.setDescription("A variety of Italian dishes");

        // Act
        Menu menu = mapper.mapFromEntity(entity);

        // Assert
        assertThat(menu).isNotNull();
        assertThat(menu.getMenuId()).isEqualTo(entity.getId());
        assertThat(menu.getName()).isEqualTo(entity.getName());
        assertThat(menu.getDescription()).isEqualTo(entity.getDescription());
        // Ignored fields cannot be checked as they are not mapped
    }
}
