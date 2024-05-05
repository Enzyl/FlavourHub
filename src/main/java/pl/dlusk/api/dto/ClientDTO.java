package pl.dlusk.api.dto;

import lombok.*;
import pl.dlusk.infrastructure.database.entity.FoodOrderEntity;
import pl.dlusk.infrastructure.security.UserEntity;

import java.util.Set;
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    String fullName;
    String phoneNumber;
}
