package pl.dlusk.domain;

import lombok.*;

@With
@Value
@Builder
@EqualsAndHashCode(of = "restaurantDeliveryAreaId")
@ToString(of = {"restaurantDeliveryAreaId"})
public class RestaurantDeliveryArea {
    Long restaurantDeliveryAreaId;

    Restaurant restaurant;

    RestaurantDeliveryStreet deliveryStreet;
}
