package pl.dlusk.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@With
@Value
@Builder
@EqualsAndHashCode(of = {"foodOrderId", "foodOrderStatus", "orderTime", "totalPrice"})
@ToString(of = {"foodOrderId", "foodOrderStatus", "orderTime", "totalPrice", "restaurant"})
public class FoodOrder {

    Long foodOrderId;
    LocalDateTime orderTime;
    String foodOrderStatus;
    BigDecimal totalPrice;
    String orderNumber;
    Client client;
    Restaurant restaurant;
    Set<OrderItem> orderItems;
    Review review;
    Delivery delivery;
    Payment payment;
}
