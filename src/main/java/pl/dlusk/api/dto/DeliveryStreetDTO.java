package pl.dlusk.api.dto;

import lombok.Data;

@Data
public class DeliveryStreetDTO {
    private String streetName;
    private String postalCode;
    private String district;
}
