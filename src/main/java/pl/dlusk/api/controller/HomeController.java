package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.Owner;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
@Slf4j
@Controller
@AllArgsConstructor
public class HomeController {
    private final RestaurantService restaurantService;
    @GetMapping("/")
    public String home() {
        log.info("HomeController - Processing home request");
        return "homeView";
    }

    @GetMapping("/login")
    public String showLoginForm(){
        return "loginView";
    }

    @GetMapping("/searchRestaurants")
    public String showRestaurantsDeliveringOnTheStreet(@RequestParam("location") String location,
                                                       @RequestParam(defaultValue = "0") int currentPage,
                                                       Model model, HttpSession session) {
        log.info("########## HomeController #### showRestaurantsDeliveringOnTheStreet #  " +
                "Attempting to show restaurants delivering to: {}", location);
        Pageable pageable = PageRequest.of(currentPage, 2);
        Page<Restaurant> restaurants = restaurantService.getRestaurantsDeliveringToArea(location, pageable);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", restaurants.getTotalPages());
        model.addAttribute("restaurants", restaurants);
        model.addAttribute("location", location);

        if (session.getAttribute("location") == null) {
            session.setAttribute("location", location);
        }
        return "restaurantsDeliveringToGivenArea";
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

        Client client = Client.builder().fullName("").phoneNumber("").foodOrders(null).user(defaultUser).build();

        Owner owner = Owner.builder().surname("").phoneNumber("").nip("").regon("").user(defaultUser).build();

        model.addAttribute("client", client);
        model.addAttribute("owner", owner);

        session.setAttribute("defaultUser", defaultUser);
        session.setAttribute("owner", owner);
        session.setAttribute("client", client);


        log.info("########## HomeController #### showRegisterForms #  FINISH");
        return "clientOwnerRegistration";
    }


}
