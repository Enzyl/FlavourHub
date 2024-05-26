package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.MenuDTO;
import pl.dlusk.api.dto.MenuItemDTO;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.Restaurant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface MenuDTOMapper {
    @Mapping(target = "menuItemDTOSet", source = "menuItems")
    MenuDTO mapToDTO(Menu menu);

    @Mapping(target = "image", ignore = true)
    MenuItemDTO mapMenuItemToDto (Menu menu);


    default Menu mapFromDTO(MenuDTO menuDTO, Restaurant restaurant) {
        return Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .restaurant(restaurant)
                .build();
    }
}
