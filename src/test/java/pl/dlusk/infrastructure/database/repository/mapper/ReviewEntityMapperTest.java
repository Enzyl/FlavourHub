package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import pl.dlusk.domain.Review;
import pl.dlusk.infrastructure.database.entity.ReviewEntity;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
class ReviewEntityMapperTest {
    private ReviewEntityMapper mapper;

    @BeforeEach
    void setup() {
        mapper = Mappers.getMapper(ReviewEntityMapper.class);
    }

    @Test
    void shouldMapFromEntity() {
        // Given
        ReviewEntity entity = new ReviewEntity();
        entity.setId(1L);
        entity.setRating(5);
        entity.setComment("Excellent");
        entity.setReviewDate(LocalDateTime.now());

        // When
        Review review = mapper.mapFromEntity(entity);

        // Then
        assertThat(review).isNotNull();
        assertThat(review.getReviewId()).isEqualTo(entity.getId());
        assertThat(review.getRating()).isEqualTo(entity.getRating());
        assertThat(review.getComment()).isEqualTo(entity.getComment());
        assertThat(review.getReviewDate()).isEqualTo(entity.getReviewDate());
        // Ignored properties should not be mapped
        assertThat(review.getFoodOrder()).isNull();
    }

    @Test
    void shouldMapToEntity() {
        // Given
        Review review = Review.builder()
                .reviewId(1L)
                .rating(5)
                .comment("Excellent")
                .reviewDate(LocalDateTime.now())
                .build();

        // When
        ReviewEntity entity = mapper.mapToEntity(review);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(review.getReviewId());
        assertThat(entity.getRating()).isEqualTo(review.getRating());
        assertThat(entity.getComment()).isEqualTo(review.getComment());
        assertThat(entity.getReviewDate()).isEqualTo(review.getReviewDate());
        // Ignored properties should not be mapped
        assertThat(entity.getFoodOrderEntity()).isNull();
    }
}