package pl.dlusk.domain;

import lombok.*;
@With
@Value
@Builder
@EqualsAndHashCode(of = "orderItemId")
@ToString(of = {"orderItemId", "quantity"})
public class OrderItem {
    Long orderItemId;
    FoodOrder foodOrder;
    MenuItem menuItem;
    Integer quantity;
}