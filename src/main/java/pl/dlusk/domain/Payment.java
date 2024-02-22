package pl.dlusk.domain;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@With
@Value
@Builder
@EqualsAndHashCode(of = "paymentId")
@ToString(of = {"paymentId", "paymentMethod", "paymentStatus","paymentTime"})
public class Payment {
    Long paymentId;
    FoodOrder foodOrder;
    String paymentMethod;
    String paymentStatus;
    LocalDateTime paymentTime;
    BigDecimal paymentAmount;
}