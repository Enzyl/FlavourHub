package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FoodOrderEntityMapper {
    @Mapping(target = "client", ignore = true)
    @Mapping(target = "restaurant", ignore = true)
    @Mapping(target = "orderItems", ignore = true)
    @Mapping(target = "review", ignore = true)
    @Mapping(target = "delivery", ignore = true)
    @Mapping(target = "payment", ignore = true)
    @Mapping(source = "id", target = "foodOrderId")
    @Mapping(source = "status", target = "foodOrderStatus")
    FoodOrder mapFromEntity(FoodOrderEntity entity);

    @Mapping(source = "foodOrderId", target = "id")
    @Mapping(source = "foodOrderStatus", target = "status")

    FoodOrderEntity mapToEntity(FoodOrder foodOrder);

}
