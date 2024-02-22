package pl.dlusk.api.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.Restaurant;

import java.util.List;

@Controller
@AllArgsConstructor
public class HomeController {
    private final RestaurantService restaurantService;
    @GetMapping("/")
    public String home(Model model) {
        // Tutaj możesz dodać atrybuty do modelu, jeśli chcesz przekazać dane do widoku
        // model.addAttribute("nazwaAtrybutu", wartośćAtrybutu);

        // Zwróć nazwę widoku, który chcesz wyświetlić
        return "HomeView";
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
}
