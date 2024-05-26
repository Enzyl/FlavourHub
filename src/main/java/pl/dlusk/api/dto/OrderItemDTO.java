package pl.dlusk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.MenuItem;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemDTO {
    FoodOrderDTO foodOrder;
    MenuItemDTO menuItem;
    Integer quantity;
}
