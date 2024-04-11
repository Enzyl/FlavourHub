package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.Owner;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

import java.util.Enumeration;
import java.util.List;
@Slf4j
@Controller
@AllArgsConstructor
public class HomeController {
    private final RestaurantService restaurantService;
    private final FoodOrderingAppUserRepository foodOrderingAppUserRepository;
    @GetMapping("/")
    public String home(Model model, HttpSession session) {
        log.info("########## HomeController #### home #  START");
        return "homeView";
    }

    @GetMapping("/searchRestaurants")
    public String showRestaurantsDeliveringOnTheStreet(@RequestParam("location") String location, Model model) {
        // Wywołanie metody serwisu, aby znaleźć restauracje dostarczające do podanej lokalizacji
        List<Restaurant> restaurants = restaurantService.getRestaurantsDeliveringToArea(location);

        // Dodanie listy restauracji do modelu, aby była dostępna w widoku
        model.addAttribute("restaurants", restaurants);

        // Zwrócenie nazwy widoku (np. "restaurants.html"), który będzie wyświetlał listę restauracji
        return "restaurantsDeliveringToGivenArea"; // Załóżmy, że masz plik restaurants.html w katalogu resources/templates
    }

    @GetMapping("/registration")
    public String showRegisterForms(HttpSession session, Model model) {
        log.info("########## HomeController #### showRegisterForms #  START");
        FoodOrderingAppUser defaultUser = FoodOrderingAppUser.builder()
                .username("")
                .password("")
                .email("")
                .role("")
                .enabled(true)
                .build();

        Client client = Client.builder()
                .fullName("")
                .phoneNumber("")
                .foodOrders(null)
                .user(defaultUser)
                .build();

        Owner owner = Owner.builder()
                .surname("")
                .phoneNumber("")
                .nip("")
                .regon("")

                .user(defaultUser)
                .build();

        // Dodaj obiekty do modelu
        model.addAttribute("client", client);
        model.addAttribute("owner", owner);

        session.setAttribute("defaultUser", defaultUser);
        session.setAttribute("owner", owner);
        session.setAttribute("client", client);

        // Zwróć nazwę widoku, który zawiera formularze rejestracji
        log.info("########## HomeController #### showRegisterForms #  FINISH");
        return "clientOwnerRegistration"; // Nazwa pliku widoku z formularzami rejestracji
    }

    @GetMapping("/login")
    public String showLoginForm(Model model){




        return "loginView";
    }
    @GetMapping("/userInfo")
    public String userInfo(Model model, HttpSession session) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        model.addAttribute("username", userDetails.getUsername());
        model.addAttribute("authorities", userDetails.getAuthorities());
        model.addAttribute("sessionID", session.getId());

        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            model.addAttribute(attributeName, session.getAttribute(attributeName));
        }

        return "userInfoView";
    }

}
