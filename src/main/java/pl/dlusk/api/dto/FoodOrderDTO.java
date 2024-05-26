package pl.dlusk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.dlusk.domain.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FoodOrderDTO {
    LocalDateTime orderTime;
    String foodOrderStatus;
    BigDecimal totalPrice;
    String orderNumber;
}
