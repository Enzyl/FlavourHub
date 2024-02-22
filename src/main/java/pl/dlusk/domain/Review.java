package pl.dlusk.domain;

import lombok.*;

import java.time.LocalDateTime;
@With
@Value
@Builder
@EqualsAndHashCode(of = "reviewId")
@ToString(of = {"reviewId", "rating", "comment","reviewDate"})
public class Review {
    Long reviewId;
    FoodOrder foodOrder;
    Integer rating;
    String comment;
    LocalDateTime reviewDate;
}
