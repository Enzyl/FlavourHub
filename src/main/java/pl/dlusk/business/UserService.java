package pl.dlusk.business;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.domain.Roles;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.util.Map;
@Service
@AllArgsConstructor
public class UserService {
    private final FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    public FoodOrderingAppUser createUserFromParams(Map<String, String> params) {
        return FoodOrderingAppUser.builder()
                .username(params.get("user.username"))
                .password(params.get("user.password"))
                .email(params.get("user.email"))
                .role(Roles.CLIENT.toString())
                .enabled(Boolean.parseBoolean(params.get("user.enabled")))
                .build();
    }
    public FoodOrderingAppUser getUserByUsername(String username) {
        FoodOrderingAppUser user = foodOrderingAppUserRepository.findByUsername(username);
        return user;
    }

}
