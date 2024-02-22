package pl.dlusk.domain;

import lombok.*;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

@With
@Value
@Builder
@EqualsAndHashCode(of = "ownerId")
@ToString(of = {"ownerId", "name", "surname"})
public class Owner {
    Long ownerId;
    String name;
    String surname;
    String phoneNumber;
    String nip;
    String regon;
    FoodOrderingAppUser user;
}
