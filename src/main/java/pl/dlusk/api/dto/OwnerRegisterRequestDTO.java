package pl.dlusk.api.dto;

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
    private String nip;
    private String regon;
    private UserDTO userDTO;

    @Builder
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserDTO {
        private String username;
        private String email;
        private String password;
        private String role;
        private boolean enabled;
    }


}
