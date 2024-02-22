package pl.dlusk.infrastructure.database.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "owner")
public class OwnerEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false, length = 255)
    private String surname;

    @Column(name = "phone_number", nullable = false, length = 15)
    private String phoneNumber;

    @Column(nullable = false, length = 10)
    private String nip;

    @Column(nullable = false, length = 14)
    private String regon;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private FoodOrderingAppUserEntity user;

}
