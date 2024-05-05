package pl.dlusk.api.dto;

import lombok.*;

import java.util.List;
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor(staticName = "of")
public class RestaurantsDTO {
    List<RestaurantDTO> restaurantDTOList;
}
