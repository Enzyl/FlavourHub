package pl.dlusk.infrastructure.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class FoodOrderingAppUserDetailsService implements UserDetailsService {

    private final FoodOrderingAppUserJpaRepository foodOrderingAppUserJpaRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        FoodOrderingAppUserEntity user = foodOrderingAppUserJpaRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        List<GrantedAuthority> authorities = getUserAuthority(user.getRole());

        return buildUserForAuthentication(user, authorities);
    }

    private List<GrantedAuthority> getUserAuthority(String role) {
        GrantedAuthority authority = new SimpleGrantedAuthority(role);
        return List.of(authority);
    }

    private UserDetails buildUserForAuthentication(FoodOrderingAppUserEntity user, List<GrantedAuthority> authorities) {
        return new FoodOrderingAppUser(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                user.getEmail(),
                user.getRole(),
                user.getEnabled(),
                authorities
        );
    }

}
