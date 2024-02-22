package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Delivery;
import pl.dlusk.infrastructure.database.entity.DeliveryEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface DeliveryEntityMapper {

    @Mapping(target = "foodOrder", ignore = true)
    Delivery mapFromEntity(DeliveryEntity entity);

    DeliveryEntity mapToEntity(Delivery delivery);
}
