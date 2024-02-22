package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.RestaurantDeliveryStreet;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryStreetEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface RestaurantDeliveryStreetEntityMapper {

    @Mapping(source = "id", target = "restaurantDeliveryStreetId")
    @Mapping(target = "deliveryAreas", ignore = true)
    RestaurantDeliveryStreet mapFromEntity (RestaurantDeliveryStreetEntity entity);
    @Mapping(source = "restaurantDeliveryStreetId", target = "id")
    @Mapping(source = "streetName", target = "streetName")
    @Mapping(source = "postalCode", target = "postalCode")
    @Mapping(source = "district", target = "district")
    @Mapping(target = "deliveryAreas", ignore = true)
    RestaurantDeliveryStreetEntity mapToEntity(RestaurantDeliveryStreet street);
}
