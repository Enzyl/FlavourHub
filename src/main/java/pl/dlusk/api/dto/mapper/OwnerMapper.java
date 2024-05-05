package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.OwnerDTO;
import pl.dlusk.domain.Owner;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface OwnerMapper {
    OwnerDTO mapToDTO(Owner owner);

}
