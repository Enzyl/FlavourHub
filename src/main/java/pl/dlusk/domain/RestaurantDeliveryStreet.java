package pl.dlusk.domain;

import lombok.*;

import java.util.Set;
@With
@Value
@Builder
@EqualsAndHashCode(of = "restaurantDeliveryStreetId")
@ToString(of = {"restaurantDeliveryStreetId", "streetName", "postalCode","district"})
public class RestaurantDeliveryStreet {
    Long restaurantDeliveryStreetId;
    String streetName;
    String postalCode;
    String district;
    Set<RestaurantDeliveryArea> deliveryAreas;
}
