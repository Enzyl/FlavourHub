package pl.dlusk.api.dto;

import lombok.*;
import pl.dlusk.domain.FoodOrder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDTO {
    String paymentMethod;
    String paymentStatus;
    LocalDateTime paymentTime;
    BigDecimal paymentAmount;
}
