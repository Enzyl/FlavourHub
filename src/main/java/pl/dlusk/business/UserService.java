package pl.dlusk.business;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.domain.Roles;
import pl.dlusk.infrastructure.security.User;
import pl.dlusk.infrastructure.security.UserRepository;

import java.util.Map;
@Service
@AllArgsConstructor
public class UserService {
    private final UserRepository foodOrderingAppUserRepository;
    public User createUserFromParams(Map<String, String> params) {
        return User.builder()
                .username(params.get("user.username"))
                .password(params.get("user.password"))
                .email(params.get("user.email"))
                .role(Roles.CLIENT.toString())
                .enabled(Boolean.parseBoolean(params.get("user.enabled")))
                .build();
    }
    public User getUserByUsername(String username) {
        User user = foodOrderingAppUserRepository.findByUsername(username);
        return user;
    }

}
