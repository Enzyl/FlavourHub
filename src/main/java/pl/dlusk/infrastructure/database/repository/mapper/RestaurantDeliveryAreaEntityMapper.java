package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.domain.RestaurantDeliveryArea;
import pl.dlusk.domain.RestaurantDeliveryStreet;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryAreaEntity;
import pl.dlusk.infrastructure.database.entity.RestaurantDeliveryStreetEntity;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RestaurantDeliveryAreaEntityMapper {
    @Mapping(source = "id", target = "restaurantDeliveryAreaId")
    @Mapping(source = "restaurantEntity", target = "restaurant", qualifiedByName = "mapRestaurantFromEntityWithIdOnly")
    @Mapping(source = "deliveryStreet",target = "deliveryStreet", qualifiedByName = "mapDeliveryStreetWithIdOnly")
    RestaurantDeliveryArea mapFromEntity(RestaurantDeliveryAreaEntity entity);


    @Named("mapRestaurantFromEntityWithIdOnly")
    @Mapping(source = "id", target = "restaurantId") // Ustawienie target na odpowiednie pole w klasie Restaurant
    default Restaurant mapRestaurantFromEntityWithIdOnly(RestaurantEntity restaurantEntity) {
        if (restaurantEntity == null) {
            return null;
        }
        return Restaurant.builder()
                .restaurantId(restaurantEntity.getId())
                .build();
    }

    @Named("mapDeliveryStreetWithIdOnly")
    @Mapping(source = "id", target = "restaurantDeliveryStreetId") // Ustawienie target na odpowiednie pole w klasie RestaurantDeliveryStreet
    default RestaurantDeliveryStreet mapDeliveryStreetWithIdOnly(RestaurantDeliveryStreetEntity entity) {
        if (entity == null) {
            return null;
        }
        return RestaurantDeliveryStreet.builder()
                        .restaurantDeliveryStreetId(entity.getId())
                                .build();
    }

    RestaurantDeliveryAreaEntity mapToEntity(RestaurantDeliveryArea rda);

}
