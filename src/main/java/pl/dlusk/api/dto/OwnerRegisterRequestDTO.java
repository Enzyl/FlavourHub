package pl.dlusk.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerRegisterRequestDTO {
    private String name;
    private String surname;
    private String phoneNumber;

    @NotBlank
    @Pattern(regexp = "\\d{10}", message = "NIP should consist of exactly 10 digits")
    private String nip;

    @NotBlank
    @Pattern(regexp = "\\d{9}|\\d{14}", message = "REGON should consist of 9 or 14 digits")
    private String regon;

    private UserDTO userDTO;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private String username;

        @NotBlank
        @Email(message = "Email should be valid")
        private String email;
        private String password;
        private String role;
        private boolean enabled;
    }
}
