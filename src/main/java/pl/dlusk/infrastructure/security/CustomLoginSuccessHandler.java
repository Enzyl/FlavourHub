package pl.dlusk.infrastructure.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import java.io.IOException;

@Component
@Slf4j

public class CustomLoginSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        HttpSession session = request.getSession();

        if (authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            session.setAttribute("username", userDetails.getUsername());
        }

        log.info("########## CustomLoginSuccessHandler ##### onAuthenticationSuccess ");

        boolean isOwner = authentication.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("OWNER"));

        if (isOwner) {
            log.info("########## CustomLoginSuccessHandler ##### onAuthenticationSuccess ##### isOwner ### TRUE");
            response.sendRedirect("/showOwnerLoggedInView");
        } else {
            log.info("########## CustomLoginSuccessHandler ##### onAuthenticationSuccess ##### isOwner ### FALSE");
            response.sendRedirect("/clientLoggedInView");
        }
    }
}
