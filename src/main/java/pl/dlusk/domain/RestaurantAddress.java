package pl.dlusk.domain;

import lombok.*;

@With
@Value
@Builder
@EqualsAndHashCode(of = "restaurantAddressId")
@ToString(of = {"restaurantAddressId", "city", "postalCode"})
public class RestaurantAddress {
    Long restaurantAddressId;
    String city;
    String postalCode;
    String address;
}
