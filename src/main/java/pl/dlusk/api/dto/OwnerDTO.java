package pl.dlusk.api.dto;

import lombok.*;

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
