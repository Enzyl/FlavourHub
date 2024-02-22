package pl.dlusk.infrastructure.database.entity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "review")
public class ReviewEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private FoodOrderEntity foodOrderEntity;

    @Column(nullable = false)
    private Integer rating;

    @Column
    private String comment;

    @Column(name = "review_date", nullable = false)
    private LocalDateTime reviewDate;
}
