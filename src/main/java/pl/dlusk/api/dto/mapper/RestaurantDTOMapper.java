package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.RestaurantDTO;
import pl.dlusk.domain.Restaurant;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface RestaurantDTOMapper {
    RestaurantDTO mapToDTO(Restaurant restaurant);
}
