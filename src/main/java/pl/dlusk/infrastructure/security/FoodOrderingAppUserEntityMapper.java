package pl.dlusk.infrastructure.security;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface FoodOrderingAppUserEntityMapper {
    @Mapping(target = "password",ignore = true)
    FoodOrderingAppUser mapFromEntity(FoodOrderingAppUserEntity entity);

    FoodOrderingAppUserEntity mapToEntity(FoodOrderingAppUser user);
}
