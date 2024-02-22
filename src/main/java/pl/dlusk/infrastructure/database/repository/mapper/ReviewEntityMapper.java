package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Review;
import pl.dlusk.infrastructure.database.entity.ReviewEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface ReviewEntityMapper {
    @Mapping(target = "foodOrder", ignore = true)
    @Mapping(source = "id", target = "reviewId")
    Review mapFromEntity(ReviewEntity entity);

    @Mapping(target = "foodOrderEntity", ignore = true)
    @Mapping(source = "reviewId", target = "id")
    ReviewEntity mapToEntity(Review entity);
}
