package pl.dlusk.infrastructure.database.repository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.domain.*;
import pl.dlusk.domain.exception.ResourceNotFoundException;
import pl.dlusk.infrastructure.database.entity.*;
import pl.dlusk.infrastructure.database.repository.jpa.*;
import pl.dlusk.infrastructure.database.repository.mapper.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Repository
@AllArgsConstructor
public class FoodOrderRepository implements FoodOrderDAO {

    private final FoodOrderJpaRepository foodOrderJpaRepository;
    private final FoodOrderEntityMapper foodOrderEntityMapper;

    private final ReviewJpaRepository reviewJpaRepository;
    private final ReviewEntityMapper reviewEntityMapper;

    private final ClientEntityMapper clientEntityMapper;

    private final RestaurantEntityMapper restaurantEntityMapper;

    private final DeliveryJpaRepository deliveryJpaRepository;
    private final DeliveryEntityMapper deliveryEntityMapper;

    private final PaymentEntityMapper paymentEntityMapper;
    private final PaymentJpaRepository paymentJpaRepository;

    private final OrderItemEntityMapper orderItemEntityMapper;
    private final OrderItemsJpaRepository orderItemsJpaRepository;

    private final MenuItemEntityMapper menuItemEntityMapper;

    @Override
    @Transactional
    public FoodOrder save(FoodOrder foodOrder) {
        FoodOrderEntity foodOrderEntity = foodOrderEntityMapper.mapToEntity(foodOrder);

        Client client = foodOrder.getClient();
        ClientEntity clientEntity = clientEntityMapper.mapToEntity(client);

        Restaurant restaurant = foodOrder.getRestaurant();
        RestaurantEntity restaurantEntity = restaurantEntityMapper.mapToEntity(restaurant);


        foodOrderEntity.setClientEntity(clientEntity);
        foodOrderEntity.setRestaurantEntity(restaurantEntity);

        FoodOrderEntity savedFoodOrderEntity = foodOrderJpaRepository.save(foodOrderEntity);

        FoodOrder savedFoodOrder = foodOrderEntityMapper.mapFromEntity(savedFoodOrderEntity);

        Delivery delivery = foodOrder.getDelivery();
        DeliveryEntity deliveryEntity = deliveryEntityMapper.mapToEntity(delivery);
        if (deliveryEntity != null) {
            deliveryEntity.setFoodOrderEntity(savedFoodOrderEntity);
            deliveryJpaRepository.save(deliveryEntity);
        }

        Payment payment = foodOrder.getPayment();
        if (payment != null) {
            PaymentEntity paymentEntity = paymentEntityMapper.mapToEntity(payment);
            paymentEntity.setFoodOrderEntity(savedFoodOrderEntity);
            paymentJpaRepository.save(paymentEntity);
        }

        Set<OrderItem> orderItems = foodOrder.getOrderItems();
        Set<OrderItemEntity> orderItemEntities = new HashSet<>();
        log.info("########## FoodOrderRepository #### save # orderItems " + orderItems);
        if (orderItems != null) {
            orderItemEntities = orderItems.stream()
                    .map(orderItem -> {
                        OrderItemEntity orderItemEntity = orderItemEntityMapper.mapToEntity(orderItem);
                        orderItemEntity.setFoodOrderEntity(savedFoodOrderEntity);
                        log.info("########## FoodOrderRepository #### save # savedFoodOrderEntity " + savedFoodOrderEntity);
                        orderItemEntity.setMenuItemEntity(menuItemEntityMapper.mapToEntity(orderItem.getMenuItem()));
                        log.info("########## FoodOrderRepository #### save # orderItem " + orderItemEntity);
                        return orderItemEntity;
                    })
                    .collect(Collectors.toSet());
        }
        log.info("########## FoodOrderRepository #### save # orderItemEntities " + orderItemEntities);


        orderItemsJpaRepository.saveAll(orderItemEntities);

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



        boolean exists = foodOrderJpaRepository.existsById(id);
        if (!exists) {
            throw new ResourceNotFoundException("FoodOrder with id " + id + " not found.");
        }


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
        List<FoodOrderEntity> foodOrderEntityList = foodOrderJpaRepository.findByOrderTimeBetween(start, end);
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

    @Override
    public Set<OrderItem> findOrderItemsByFoodOrderId(Long foodOrderId) {

        List<OrderItemEntity> orderItemEntities = orderItemsJpaRepository.findByFoodOrderEntityId(foodOrderId);
        log.info("########## FoodOrderRepository #### findOrderItemsByFoodOrderId for foodOrderId [" + foodOrderId + "] #  orderItemEntities: " + orderItemEntities);


        Optional<FoodOrderEntity> foodOrderEntityOpt = foodOrderJpaRepository.findById(foodOrderId);
        if (foodOrderEntityOpt.isEmpty()) {
            throw new ResourceNotFoundException("FoodOrder not found with id: " + foodOrderId);
        }
        FoodOrder foodOrder = foodOrderEntityMapper.mapFromEntity(foodOrderEntityOpt.get());


        return orderItemEntities.stream()
                .map(orderItemEntity -> {
                    MenuItem menuItem = menuItemEntityMapper.mapFromEntity(orderItemEntity.getMenuItemEntity());
                    OrderItem orderItem = orderItemEntityMapper.mapFromEntity(orderItemEntity);
                    return orderItem.withMenuItem(menuItem).withFoodOrder(foodOrder);
                })
                .collect(Collectors.toSet());
    }

    @Override
    public void updateFoodOrderStatus(Long orderId, String status) {
        foodOrderJpaRepository.updateFoodOrderStatus(orderId,status);
    }

    @Override
    public FoodOrder findFoodOrderByFoodOrderNumber(String foodOrderNumber) {
        FoodOrderEntity foodOrderByOrderNumber = foodOrderJpaRepository.findByOrderNumber(foodOrderNumber);
        return foodOrderEntityMapper.mapFromEntity(foodOrderByOrderNumber);
    }


}
