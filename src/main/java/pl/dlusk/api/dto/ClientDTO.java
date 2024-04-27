package pl.dlusk.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    String fullName;
    String phoneNumber;
    UserDTO userDTO;


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
