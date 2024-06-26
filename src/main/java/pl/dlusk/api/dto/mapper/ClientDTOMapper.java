package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.ClientDTO;
import pl.dlusk.domain.Client;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientDTOMapper {
    ClientDTO mapToDTO(Client client);
}
