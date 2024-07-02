package pl.dlusk.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientRegisterRequestDTO {
    String fullName;
    @NotBlank
    @Pattern(regexp = "\\d{9}", message = "NIP should consist of exactly 10 digits")
    String phoneNumber;
    UserDTO userDTO;


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
