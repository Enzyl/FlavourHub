package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Client;
import pl.dlusk.infrastructure.database.entity.ClientEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientEntityMapper {

    @Mapping(source = "id", target = "clientId")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "foodOrders", ignore = true)
    Client mapFromEntity(ClientEntity entity);
    @Mapping(source = "clientId", target = "id")
    ClientEntity mapToEntity(Client client);


}
