package pl.dlusk.infrastructure.security;

import lombok.*;

@With
@Value
@Builder
@EqualsAndHashCode(of = "username")
@ToString(of = {"username", "email", "role", "enabled"})
public class FoodOrderingAppUser {
    Long userId;
    String username;
    String password;
    String email;
    String role;
    Boolean enabled;
}
