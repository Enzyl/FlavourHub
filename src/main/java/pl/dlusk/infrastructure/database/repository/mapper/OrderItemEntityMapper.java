package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.OrderItem;
import pl.dlusk.infrastructure.database.entity.OrderItemEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemEntityMapper {
    @Mapping(target = "foodOrder", ignore = true)
    @Mapping(target = "menuItem", ignore = true)
    @Mapping(source = "id", target = "orderItemId")
    OrderItem mapFromEntity(OrderItemEntity entity);
    @Mapping(source = "orderItemId", target = "id")
    @Mapping(target = "foodOrderEntity", ignore = true)
    @Mapping(target = "menuItemEntity", ignore = true)
    OrderItemEntity mapToEntity(OrderItem orderItem);

}
