package pl.dlusk.domain;

import lombok.Builder;
import lombok.ToString;
import lombok.Value;
import lombok.With;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Value
@Builder
@With
@ToString(of = "customerFoodOrders")
public class ClientOrderHistory {
    Long customerId;
    List<FoodOrderRequest> customerFoodOrders;


    @Value
    @Builder
    @ToString(of = {"orderTime", "foodOrderStatus","totalPrice", "restaurant", "orderItems", "payment"})
    public static class FoodOrderRequest {
        Long orderId;
        LocalDateTime orderTime;
        String foodOrderStatus;
        BigDecimal totalPrice;
        Restaurant restaurant;
        Set<OrderItem> orderItems;
        Payment payment;

        public boolean isCancellable() {
            // Ustawienie limitu anulowania na 20 minut
            final long CANCELLATION_LIMIT_MINUTES = 20;
            return Duration.between(this.orderTime, LocalDateTime.now()).toMinutes() < CANCELLATION_LIMIT_MINUTES;
        }
    }
}
