package pl.dlusk.infrastructure.database.repository;

import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.Review;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
import pl.dlusk.infrastructure.database.entity.RestaurantEntity;
import pl.dlusk.infrastructure.database.entity.ReviewEntity;
import pl.dlusk.infrastructure.database.repository.jpa.FoodOrderJpaRepository;
import pl.dlusk.infrastructure.database.repository.jpa.ReviewJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.FoodOrderEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.ReviewEntityMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@AllArgsConstructor
public class FoodOrderRepository implements FoodOrderDAO {

    private final FoodOrderJpaRepository foodOrderJpaRepository;
    private final FoodOrderEntityMapper foodOrderEntityMapper;

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewEntityMapper reviewEntityMapper;

    @Override
    public FoodOrder save(FoodOrder foodOrder) {
        FoodOrderEntity foodOrderEntity = foodOrderEntityMapper.mapToEntity(foodOrder);

        // Zapis lub aktualizacja encji w bazie danych
        FoodOrderEntity savedEntity = foodOrderJpaRepository.save(foodOrderEntity);
        FoodOrder savedFoodOrder = foodOrderEntityMapper.mapFromEntity(savedEntity);

        return savedFoodOrder;
    }

    @Override
    public Optional<FoodOrder> findById(Long foodOrderId) {
        Optional<FoodOrderEntity> foodOrderEntityOptById = foodOrderJpaRepository.findById(foodOrderId);

        if (foodOrderEntityOptById.isEmpty()) {
            return Optional.empty();
        }

        FoodOrder foodOrder = foodOrderEntityMapper.mapFromEntity(foodOrderEntityOptById.get());

        return Optional.of(foodOrder);
    }

    @Override
    public List<FoodOrder> findAll() {
        List<FoodOrderEntity> all = foodOrderJpaRepository.findAll();
        List<FoodOrder> allFoodOrders = all.
                stream().
                map(foodOrderEntityMapper::mapFromEntity).
                collect(Collectors.toList());
        return allFoodOrders;
    }

    @Override
    public List<FoodOrder> findByClientId(Long clientId) {
        List<FoodOrderEntity> foodOrderEntities = foodOrderJpaRepository.findByClientEntityId(clientId);
        return foodOrderEntities.stream()
                .map(foodOrderEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoodOrder> findByRestaurantId(Long restaurantId) {
        List<FoodOrderEntity> foodOrderEntities = foodOrderJpaRepository.findByRestaurantEntityId(restaurantId);
        return foodOrderEntities.stream()
                .map(foodOrderEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteById(Long id) {
        // Opcjonalnie, możesz dodać logikę sprawdzającą, czy zamówienie o danym ID istnieje,
        // aby rzucić wyjątek, jeśli nie. To zapewni lepszą informację zwrotną dla użytkownika
        // lub serwisu wywołującego tę metodę, gdy próbuje usunąć nieistniejące zamówienie.
        boolean exists = foodOrderJpaRepository.existsById(id);
        if (!exists) {
            throw new ResourceNotFoundException("FoodOrder with id " + id + " not found.");
        }

        // Usuwanie zamówienia o podanym ID
        foodOrderJpaRepository.deleteById(id);
    }

    @Override
    public List<FoodOrder> findByOrderStatus(String status) {
        List<FoodOrderEntity> foodOrderEntitiesByStatus = foodOrderJpaRepository.findByStatus(status);
        return foodOrderEntitiesByStatus.stream()
                .map(foodOrderEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<FoodOrder> findByDateRange(LocalDateTime start, LocalDateTime end) {
        List<FoodOrderEntity> foodOrderEntityList = foodOrderJpaRepository.findByOrderTimeBetween(start,end);
        return foodOrderEntityList.stream()
                .map(foodOrderEntityMapper::mapFromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Review addReview(Long orderId, Review review) {
        FoodOrderEntity foodOrderEntity = foodOrderJpaRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("FoodOrder not found with id: " + orderId));

        ReviewEntity reviewEntity = reviewEntityMapper.mapToEntity(review);

        reviewEntity.setFoodOrderEntity(foodOrderEntity);

        ReviewEntity savedReviewEntity = reviewJpaRepository.save(reviewEntity);

        return reviewEntityMapper.mapFromEntity(savedReviewEntity);
    }


}
