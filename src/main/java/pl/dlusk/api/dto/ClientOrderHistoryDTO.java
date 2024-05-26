package pl.dlusk.api.dto;

import lombok.*;
import pl.dlusk.domain.ClientOrderHistory;
import pl.dlusk.domain.OrderItem;
import pl.dlusk.domain.Payment;
import pl.dlusk.domain.Restaurant;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(of = "customerFoodOrders")
public class ClientOrderHistoryDTO {
    List<ClientOrderHistoryDTO.FoodOrderRequestDTO> customerFoodOrders;


    @Value
    @Builder
    @ToString(of = {"orderTime", "foodOrderStatus","totalPrice", "restaurant", "orderItems", "payment"})
    public static class FoodOrderRequestDTO {
        LocalDateTime orderTime;
        String foodOrderStatus;
        BigDecimal totalPrice;
        RestaurantDTO restaurant;
        Set<OrderItemDTO> orderItems;
        PaymentDTO payment;
    }
}
