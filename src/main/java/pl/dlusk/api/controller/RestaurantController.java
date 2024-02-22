package pl.dlusk.api.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.MenuItem;

import java.util.HashSet;
import java.util.Set;

@Slf4j
@Controller
@AllArgsConstructor
public class RestaurantController {
    private final RestaurantDAO restaurantDAO;
    @GetMapping("/restaurantMenu/{restaurantId}")
    public String showRestaurantMenu(@PathVariable Long restaurantId, Model model){
        log.info("########## RestaurantController ##### showRestaurantMenu #### restaurantId: " + restaurantId );
        try {
            Menu menu = restaurantDAO.getMenuRestaurantById(restaurantId);
            log.info("########## menu : " + menu.toString() );
            Set<MenuItem> menuItemsByMenuId = restaurantDAO.findMenuItemsByMenuId(menu.getMenuId());
            log.info("########## menuItemsByMenuId : " + menuItemsByMenuId);
            menu = menu.withMenuItems(menuItemsByMenuId);
            if (menu != null && menu.getMenuItems() == null) {
               menu = menu.withMenuItems(new HashSet<>());
            }
            model.addAttribute("menu", menu);
            model.addAttribute("restaurantId", restaurantId);
            return "restaurantMenu";
        }catch (Exception e){
            model.addAttribute("errorMessage", "Menu dla restauracji o ID " + restaurantId + " nie zostało znalezione.");
            return "errorPage";
        }
    }
}
