package pl.dlusk.api.dto;

import lombok.Data;

@Data
public class FoodOrderingAppUserDTO {

    private String username;
    private String password;
    private String email;
    private boolean enabled;
    private String fullName;
    private String role;
    private String phoneNumber;

        // Getters and setters

}
