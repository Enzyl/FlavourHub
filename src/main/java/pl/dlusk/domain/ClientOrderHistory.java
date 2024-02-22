package pl.dlusk.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Value
@Builder
@ToString(of = "customerFoodOrders")
public class ClientOrderHistory {
    Long customerId;
    List<FoodOrderRequest> customerFoodOrders;


    @Value
    @Builder
    @ToString(of = {"orderTime", "foodOrderStatus","totalPrice"})
    public static class FoodOrderRequest {
        LocalDateTime orderTime;
        String foodOrderStatus;
        BigDecimal totalPrice;
        Restaurant restaurant;
        Set<OrderItem> orderItems;
        Payment payment;
    }
}
