package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.database.entity.OwnerEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OwnerEntityMapper {
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "id", target = "ownerId")
    Owner mapFromEntity(OwnerEntity entity);
    @Mapping(source = "ownerId", target = "id")
    OwnerEntity mapToEntity(Owner owner);

}
