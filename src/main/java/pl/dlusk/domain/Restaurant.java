package pl.dlusk.domain;

import lombok.*;

@With
@Value
@Builder
@EqualsAndHashCode(of = "restaurantId")
@ToString(of = {"restaurantId", "name", "description"})
public class Restaurant {
    Long restaurantId;
    String name;
    String description;
    String imagePath;
    RestaurantAddress address;
    Owner owner;
}