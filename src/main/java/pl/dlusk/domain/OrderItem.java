package pl.dlusk.domain;

import lombok.*;
@With
@Value
@Builder
@EqualsAndHashCode(of = {"orderItemId", "menuItem","quantity"})
@ToString(of = {"quantity","foodOrder","menuItem"})
public class OrderItem {
    Long orderItemId;
    FoodOrder foodOrder;
    MenuItem menuItem;
    Integer quantity;
}