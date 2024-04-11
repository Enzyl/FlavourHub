package pl.dlusk.domain;

import lombok.*;

@With
@Value
@Builder
@EqualsAndHashCode(of = {"restaurantDeliveryAreaId","restaurant","deliveryStreet"})
@ToString(of = {"restaurantDeliveryAreaId","restaurant","deliveryStreet"})
public class RestaurantDeliveryArea {
    Long restaurantDeliveryAreaId;

    Restaurant restaurant;

    RestaurantDeliveryStreet deliveryStreet;
}
