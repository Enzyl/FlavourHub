package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.MenuItem;
import pl.dlusk.infrastructure.database.entity.MenuItemEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface MenuItemEntityMapper {

    @Mapping(source = "id", target = "menuItemId")
    @Mapping(source = "name", target = "name")
    @Mapping(target = "menu", ignore = true)
    MenuItem mapFromEntity(MenuItemEntity entity);
    @Mapping(source = "menuItemId", target = "id")
    @Mapping(source = "name", target = "name")
    @Mapping(target = "menuEntity", ignore = true)
    MenuItemEntity mapToEntity(MenuItem menu);
}
