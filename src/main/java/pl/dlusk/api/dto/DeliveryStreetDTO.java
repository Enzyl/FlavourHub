package pl.dlusk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DeliveryStreetDTO {
    private String streetName;
    private String postalCode;
    private String district;
}
