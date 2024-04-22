package pl.dlusk.business;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.domain.shoppingCart.ShoppingCart;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserRepository;

@Service
@AllArgsConstructor
public class ShoppingCartService {

    private final FoodOrderingAppUserRepository foodOrderingAppUserRepository;

    public ShoppingCart getOrCreateShoppingCart(HttpSession session) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
        if (shoppingCart == null) {
            shoppingCart = ShoppingCart.builder().build();
            session.setAttribute("shoppingCart", shoppingCart);
        }
        return shoppingCart;
    }

    public ShoppingCart ensureShoppingCart(HttpSession session, Long restaurantId, String username) {
        ShoppingCart shoppingCart = (ShoppingCart) session.getAttribute("shoppingCart");
        if (shoppingCart == null || !shoppingCart.getRestaurantId().equals(restaurantId)) {
            Long userId = foodOrderingAppUserRepository.findIdByUsername(username);
            shoppingCart = ShoppingCart.builder()
                    .userId(userId)
                    .restaurantId(restaurantId)
                    .build();
            session.setAttribute("shoppingCart", shoppingCart);
        }
        return shoppingCart;
    }

}
