package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.ClientDTO;
import pl.dlusk.domain.Client;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface ClientDTOMapper {

    default Client mapFromDTO(ClientDTO dto) {
        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username(dto.getUserDTO().getUsername())
                .password(dto.getUserDTO().getPassword())
                .email(dto.getUserDTO().getEmail())
                .role(dto.getUserDTO().getRole())
                .enabled(dto.getUserDTO().isEnabled())
                .build();

        return Client.builder()
                .fullName(dto.getFullName())
                .phoneNumber(dto.getPhoneNumber())
                .foodOrders(null)
                .user(user)
                .build();
    }

}
