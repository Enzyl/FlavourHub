package pl.dlusk.domain.shoppingCart;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import pl.dlusk.domain.MenuItem;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@Builder
@With
public class ShoppingCart {
    private Long restaurantId;
    private Long userId;

    @Builder.Default
    private Map<MenuItem, Integer> items = new HashMap<>();

    public void addItem(MenuItem item) {
        items.merge(item, 1, Integer::sum); // Dodaj element lub zwiększ jego ilość
        log.info("Item: [" + item + "] has been added to the cart. Current state: " + items);
    }

    public void removeItem(MenuItem item) {
        items.computeIfPresent(item, (key, quantity) -> (quantity - 1 > 0) ? quantity - 1 : null); // Zmniejsz ilość lub usuń, jeśli ilość <= 0
        log.info("Item: [" + item + "] has been removed from the cart. Current state: " + items);
    }

    // Metoda do zmiany ilości przedmiotu
    public void updateItemQuantity(MenuItem item, int quantity) {
        if (quantity > 0) {
            items.put(item, quantity);
        } else {
            items.remove(item);
        }
    }
}
