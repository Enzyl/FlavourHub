package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.MenuDTO;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.Restaurant;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface MenuDTOMapper {
    default Menu convertToMenu(MenuDTO menuDTO, Restaurant restaurant) {
        return Menu.builder()
                .name(menuDTO.getName())
                .description(menuDTO.getDescription())
                .restaurant(restaurant)
                .build();
    }
}
