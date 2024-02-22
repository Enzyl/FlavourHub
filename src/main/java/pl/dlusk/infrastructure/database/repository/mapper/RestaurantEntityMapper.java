package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RestaurantEntityMapper {
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(source = "id", target = "restaurantId")
    Restaurant mapFromEntity(RestaurantEntity entity);
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "ownerEntity", ignore = true)
    @Mapping(source = "restaurantId", target = "id")
    RestaurantEntity mapToEntity(Restaurant restaurant);


}
