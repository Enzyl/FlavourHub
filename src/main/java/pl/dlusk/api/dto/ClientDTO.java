package pl.dlusk.api.dto;

import lombok.*;
@Data
@With
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientDTO {
    String fullName;
    String phoneNumber;
}
