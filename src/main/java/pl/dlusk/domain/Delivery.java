package pl.dlusk.domain;

import lombok.*;

import java.time.LocalDateTime;
@With
@Value
@Builder
@EqualsAndHashCode(of = "deliveryId")
@ToString(of = {"deliveryId", "deliveryAddress", "deliveryTime","deliveryStatus"})
public class Delivery {
    Long deliveryId;
    FoodOrder foodOrder;
    String deliveryAddress;
    LocalDateTime deliveryTime;
    String deliveryStatus;
}
