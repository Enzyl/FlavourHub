package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.RestaurantAddress;
import pl.dlusk.infrastructure.database.entity.RestaurantAddressEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface RestaurantAddressEntityMapper {
    @Mapping(source = "id", target = "restaurantAddressId")
    RestaurantAddress mapFromEntity(RestaurantAddressEntity entity);
    @Mapping(source = "restaurantAddressId", target = "id")
    RestaurantAddressEntity mapToEntity(RestaurantAddress restaurantAddress);

}
