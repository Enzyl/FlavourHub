package pl.dlusk.business;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import pl.dlusk.domain.Menu;
import pl.dlusk.domain.MenuItem;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.domain.shoppingCart.ShoppingCart;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
public class UtilService {

    private final PaymentService paymentService;
    public void updateSessionAttributes(HttpSession session, Menu menu, Restaurant restaurant) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null) {
            menuItems = new HashSet<>();
        }
        session.setAttribute("menuItems", menuItems);
        session.setAttribute("menuToUpdate", menu);
        session.setAttribute("restaurant", restaurant);
    }

    public void addModelAttributes(Model model, HttpSession session) {
        Map<String, List<MenuItem>> groupedMenuItems = groupMenuItemsByCategory(session);
        model.addAttribute("menu", session.getAttribute("menuToUpdate"));
        model.addAttribute("groupedMenuItems", groupedMenuItems);
    }
    public Map<String, List<MenuItem>> groupMenuItemsByCategory(HttpSession session) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null || menuItems.isEmpty()) {
            return new HashMap<>();
        }
        return menuItems.stream()
                .collect(Collectors.groupingBy(MenuItem::getCategory));
    }
    public Set<MenuItem> getMenuItemsFromSession(HttpSession session) {
        Set<MenuItem> menuItems = (Set<MenuItem>) session.getAttribute("menuItems");
        if (menuItems == null) {
            return new HashSet<>();
        }
        return menuItems;
    }


    public void updateModelWithMenuDetails(Model model, ShoppingCart shoppingCart, Long restaurantId, Menu menu, HttpSession session) {

        BigDecimal totalValue = paymentService.calculateTotalValue(shoppingCart);
        model.addAttribute("menu", menu);
        model.addAttribute("shoppingCart", shoppingCart);
        model.addAttribute("restaurantId", restaurantId);
        model.addAttribute("totalValue", totalValue);
        session.setAttribute("totalValue", totalValue);
        session.setAttribute("restaurantId", restaurantId);
    }


    public void clearOrderSessionAttributes(HttpSession session, String uniqueFoodNumber) {
        session.removeAttribute("delivery");
        session.removeAttribute("shoppingCart");
        session.removeAttribute("payment");
        session.removeAttribute("totalValue");
        session.removeAttribute("restaurantId");
        session.removeAttribute("location");
        session.removeAttribute("uniqueFoodNumber");
        session.setAttribute("uniqueFoodNumber", uniqueFoodNumber);
    }

}
