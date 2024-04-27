package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.OwnerDTO;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)

public interface OwnerDTOMapper {
   default  Owner mapFromDTO(OwnerDTO dto) {
        FoodOrderingAppUser user = FoodOrderingAppUser.builder()
                .username(dto.getUserDTO().getUsername())
                .password(dto.getUserDTO().getPassword())
                .email(dto.getUserDTO().getEmail())
                .role(dto.getUserDTO().getRole())
                .enabled(dto.getUserDTO().isEnabled())
                .build();

        return Owner.builder()
                .name(dto.getName())
                .surname(dto.getSurname())
                .phoneNumber(dto.getPhoneNumber())
                .nip(dto.getNip())
                .regon(dto.getRegon())
                .user(user)
                .build();
    }
}
