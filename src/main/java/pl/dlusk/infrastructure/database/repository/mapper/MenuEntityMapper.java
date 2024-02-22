package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Menu;
import pl.dlusk.infrastructure.database.entity.MenuEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface MenuEntityMapper {

    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "menuItems", ignore = true)
    @Mapping(source = "id", target = "menuId")
    Menu mapFromEntity(MenuEntity entity);

    @Mapping(source = "menuId", target = "id")
    MenuEntity mapToEntity(Menu menu);

}
