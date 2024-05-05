package pl.dlusk.infrastructure.security;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface UserEntityMapper {
    @Mapping(target = "password",ignore = true)
    User mapFromEntity(UserEntity entity);

    UserEntity mapToEntity(User user);
}
