package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.api.dto.OwnerRegisterRequestDTO;
import pl.dlusk.api.dto.mapper.OwnerRegisterDTOMapper;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.OwnerService;
import pl.dlusk.business.RestaurantService;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.security.User;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;

import java.util.Set;

@Slf4j
@Controller
@AllArgsConstructor
public class OwnerController {
    private final OwnerService ownerService;
    private final RestaurantService restaurantService;
    private final FoodOrderService foodOrderService;
    private final OwnerRegisterDTOMapper ownerRegisterDTOMapper;

    @PostMapping("/registerOwner")
    public String registerOwner(@ModelAttribute OwnerRegisterRequestDTO ownerRegisterRequestDTO,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {

        Owner owner = ownerRegisterDTOMapper.mapFromDTO(ownerRegisterRequestDTO);
        log.info("########## OwnerController #### registerOwner ### owner: " + owner);
        log.info("########## OwnerController #### registerOwner ### user: " + owner.getUser());

        session.setAttribute("owner", owner);

        try {
            ownerService.registerOwner(owner, owner.getUser());
        } catch (UsernameAlreadyExistsException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Nazwa użytkownika lub NIP właściciela już istnieje.");
            return "redirect:/registration";
        }

        return "redirect:/showOwnerLoggedInView";
    }

    @GetMapping("/showOwnerLoggedInView")
    @PreAuthorize("hasRole('ROLE_OWNER')")
    public String showOwnerLoggedInView(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        if (username == null) {
            log.info("Session expired or user not logged in.");
            return "redirect:/login";
        }

        User user = ownerService.getUserByUsername(username);
        Restaurant restaurant = restaurantService.getRestaurantByUsername(username);

        session.setAttribute("user", user);
        session.setAttribute("restaurant", restaurant);

        if (restaurant == null) {
            log.info("Restaurant not found for username: {}", username);
            return "redirect:/showRestaurantRegistrationForm";
        }

        Menu menu = restaurantService.getMenuByRestaurant(restaurant);
        if (menu == null) {
            log.info("No menu found for restaurant ID: {}", restaurant.getRestaurantId());
            return "redirect:/showAddMenuToTheRestaurantView";
        }

        Set<MenuItem> menuItems = restaurantService.getMenuItemsByMenuId(menu);
        if (menuItems.isEmpty()) {
            log.info("No menu items found for menu ID: {}", menu.getMenuId());
            return "redirect:/addItemsToTheMenuView";
        }

        model.addAttribute("menu", menu.withMenuItems(menuItems));
        model.addAttribute("restaurant", restaurant);
        model.addAttribute("ownerUsername", username);

        log.info("Owner logged in view displayed for {}", username);
        return "ownerRestaurantInfo";
    }


}
