package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.MenuItemDTO;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.MenuItem;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface MenuItemDTOMapper {

    MenuItemDTO mapToDto(MenuItem menuItem);

    default MenuItem mapDTOtoMenuItem(MenuItemDTO dto, String imageUrl, Menu menu) {
        return MenuItem.builder()
                .name(dto.getName())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .category(dto.getCategory())
                .imagePath(imageUrl)
                .menu(menu)
                .build();
    }
}
