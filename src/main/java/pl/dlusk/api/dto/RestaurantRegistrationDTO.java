package pl.dlusk.api.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RestaurantRegistrationDTO {
    @NotBlank(message = "Nazwa restauracji jest wymagana.")
    private String name;

    @NotBlank(message = "Opis jest wymagany.")
    private String description;

    private String imagePath;

    @Valid // Dodanie adnotacji @Valid, aby zapewnić kaskadową walidację pól w AddressDTO
    private AddressDTO address;

    @Data
    public static class AddressDTO {
        @NotBlank(message = "Miasto jest wymagane.")
        @Pattern(regexp = "[a-zA-Z\\s]+", message = "Nazwa miasta może zawierać tylko litery.")
        private String city;

        @NotBlank(message = "Kod pocztowy jest wymagany.")
        @Pattern(regexp = "\\d{2}-\\d{3}", message = "Kod pocztowy musi być w formacie 00-000.")
        private String postalCode;

        @NotBlank(message = "Adres jest wymagany.")
        private String address;
    }
}
