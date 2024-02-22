package pl.dlusk.business;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.Review;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
import pl.dlusk.infrastructure.database.repository.jpa.FoodOrderJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.FoodOrderEntityMapper;

import java.time.LocalDateTime;
import java.util.List;

@Service
@AllArgsConstructor
public class FoodOrderService {
    private final FoodOrderDAO foodOrderDAO;

    @Transactional
    public FoodOrder createOrUpdateFoodOrder(FoodOrder foodOrder) {
        return foodOrderDAO.save(foodOrder);
    }

    public FoodOrder getFoodOrderById(Long id) {
        // Logika do pobierania zamówienia po ID
        return foodOrderDAO.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FoodOrder with id [%s] not found".formatted(id)));
    }


    public List<FoodOrder> getAllFoodOrders() {
        // Logika do pobierania wszystkich zamówień
        return foodOrderDAO.findAll();
    }

    public List<FoodOrder> getFoodOrdersByClient(Long clientId) {
        // Logika do pobierania zamówień danego klienta
        return foodOrderDAO.findByClientId(clientId);
    }

    public List<FoodOrder> getFoodOrdersByRestaurant(Long restaurantId) {
        // Logika do pobierania zamówień dla danej restauracji
        return foodOrderDAO.findByRestaurantId(restaurantId);
    }

    public void deleteFoodOrder(Long id) {
        // Logika do usuwania zamówienia po ID
        foodOrderDAO.deleteById(id);
    }

    public List<FoodOrder> getFoodOrdersByStatus(String status) {
        // Logika do pobierania zamówień o określonym statusie
        return foodOrderDAO.findByOrderStatus(status);
    }

    public List<FoodOrder> getFoodOrdersWithinDateRange(LocalDateTime start, LocalDateTime end) {
        // Logika do pobierania zamówień w określonym przedziale czasowym
        return foodOrderDAO.findByDateRange(start, end);
    }

    public Review addReviewToRestaurant(Long orderId, Review review) {
        return foodOrderDAO.addReview(orderId,review);
    }

}
