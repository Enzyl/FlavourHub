package pl.dlusk.infrastructure.database.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "restaurant_delivery_street")
public class RestaurantDeliveryStreetEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "street_name", nullable = false, length = 255)
    private String streetName;

    @Column(name = "postal_code", nullable = false, length = 20)
    private String postalCode;

    @Column(nullable = false, length = 255)
    private String district;

    // Relationship to restaurant_delivery_area is not defined in the provided SQL
    // Assuming OneToMany if multiple areas can reference the same street
    @OneToMany(mappedBy = "deliveryStreet",fetch = FetchType.LAZY)
    private Set<RestaurantDeliveryAreaEntity> deliveryAreas;
}
