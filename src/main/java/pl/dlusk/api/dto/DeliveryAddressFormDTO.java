package pl.dlusk.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeliveryAddressFormDTO {
    @NotBlank
    @Size(max = 100)
    private String streetName;

    @NotBlank
    @Pattern(regexp = "\\d+[A-Za-z]*")
    private String buildingNumber;

    private String apartmentNumber;

    @NotBlank
    @Pattern(regexp = "\\d{2}-\\d{3}")
    private String postalCode;

    @NotBlank
    @Size(max = 100)
    private String city;

    private String additionalInstructions;
}
