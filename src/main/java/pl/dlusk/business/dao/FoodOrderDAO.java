package pl.dlusk.business.dao;

import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.Review;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface FoodOrderDAO {
    FoodOrder save(FoodOrder foodOrder); // Zapis nowego zamówienia lub aktualizacja istniejącego
    Optional<FoodOrder> findById(Long id); // Pobranie zamówienia po ID
    List<FoodOrder> findAll(); // Pobranie wszystkich zamówień
    List<FoodOrder> findByClientId(Long clientId); // Pobranie wszystkich zamówień dla danego klienta
    List<FoodOrder> findByRestaurantId(Long restaurantId); // Pobranie wszystkich zamówień dla danej restauracji
    void deleteById(Long id); // Usunięcie zamówienia po ID
    List<FoodOrder> findByOrderStatus(String status); // Pobranie zamówień o określonym statusie
    List<FoodOrder> findByDateRange(LocalDateTime start, LocalDateTime end); // Pobranie zamówień w określonym przedziale czasowym

    Review addReview(Long orderId, Review review);
}
