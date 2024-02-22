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
@Table(name = "menu")
public class MenuEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column
    private String description;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", referencedColumnName = "id")
    private RestaurantEntity restaurantEntity;

    // Relationship to menu_items is not defined in the provided SQL, assuming OneToMany
    @OneToMany(mappedBy = "menuEntity",fetch = FetchType.LAZY)
    private Set<MenuItemEntity> menuItemEntities;

}
