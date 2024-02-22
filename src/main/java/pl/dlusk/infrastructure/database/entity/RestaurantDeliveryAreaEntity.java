package pl.dlusk.infrastructure.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurant_delivery_area")
public class RestaurantDeliveryAreaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id")
    private RestaurantEntity restaurantEntity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_street_id", referencedColumnName = "id")
    private RestaurantDeliveryStreetEntity deliveryStreet;
}
