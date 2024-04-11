package pl.dlusk.infrastructure.security;

import lombok.*;

@With
@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@EqualsAndHashCode(of = "username")
@ToString(of = {"userId","username", "email", "role", "enabled"})

public class FoodOrderingAppUser {
    Long userId;
    String username;
    String password;
    String email;
    String role;
    Boolean enabled;
}
