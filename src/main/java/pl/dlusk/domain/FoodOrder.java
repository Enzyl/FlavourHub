package pl.dlusk.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
@With
@Value
@Builder
@EqualsAndHashCode(of = "foodOrderId")
@ToString(of = {"foodOrderId", "foodOrderStatus"})
public class FoodOrder {

    Long foodOrderId;
    LocalDateTime orderTime;
    String foodOrderStatus;
    BigDecimal totalPrice;
    Client client;
    Restaurant restaurant;
    Set<OrderItem> orderItems;
    Review review;
    Delivery delivery;
    Payment payment;

}
