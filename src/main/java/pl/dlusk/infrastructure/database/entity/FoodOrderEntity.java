package pl.dlusk.infrastructure.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "food_order")
public class FoodOrderEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_time", nullable = false)
    private LocalDateTime orderTime;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @OneToOne(mappedBy = "foodOrderEntity")
    private ReviewEntity reviewEntity;

    @OneToOne(mappedBy = "foodOrderEntity")
    private DeliveryEntity deliveryEntity;

    @OneToOne(mappedBy = "foodOrderEntity")
    private PaymentEntity paymentEntity;

    @OneToMany(mappedBy = "foodOrderEntity", fetch = FetchType.LAZY)
    private Set<OrderItemEntity> orderItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private ClientEntity clientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id")
    private RestaurantEntity restaurantEntity;
}
