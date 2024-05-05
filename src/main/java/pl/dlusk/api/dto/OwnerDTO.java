package pl.dlusk.api.dto;

import lombok.*;
import pl.dlusk.infrastructure.security.User;

@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerDTO {
    String name;
    String surname;
    String phoneNumber;
    String nip;
    String regon;
}
