package pl.dlusk.api.dto;

import lombok.Data;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

@Data
public class OwnerDTO {
    private String name;
    private String surname;
    private String phoneNumber;
    private String nip;
    private String regon;
    private UserDTO user;

    @Data
    public static class UserDTO {
        private String username;
        private String email;
        private String password;
        private boolean enabled;
    }

    public Owner createOwnerFromDTO(OwnerDTO dto) {
        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username(dto.getUser().getUsername())
                .password(dto.getUser().getPassword())
                .email(dto.getUser().getEmail())
                .enabled(dto.getUser().isEnabled())
                .build();

        return Owner.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .phoneNumber(dto.getPhoneNumber())
                .nip(dto.getNip())
                .regon(dto.getRegon())
                .user(user)
                .build();
    }
}
