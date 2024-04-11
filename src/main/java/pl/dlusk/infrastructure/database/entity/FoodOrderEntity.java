package pl.dlusk.infrastructure.database.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter
@ToString(of = {"id","orderTime"})
@EqualsAndHashCode(of = {"id","orderTime"})
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

    @Column(name = "order_number", nullable = false)
    private String order_number;

    @OneToOne(mappedBy = "foodOrderEntity")
    private ReviewEntity reviewEntity;

    @OneToOne(mappedBy = "foodOrderEntity")
    private DeliveryEntity deliveryEntity;

    @OneToOne(mappedBy = "foodOrderEntity")
    private PaymentEntity paymentEntity;

    @OneToMany(mappedBy = "foodOrderEntity", fetch = FetchType.LAZY)
    private Set<OrderItemEntity> menuItems;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", referencedColumnName = "id")
    private ClientEntity clientEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id")
    private RestaurantEntity restaurantEntity;
}
