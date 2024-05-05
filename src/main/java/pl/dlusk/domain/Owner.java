package pl.dlusk.domain;

import lombok.*;
import pl.dlusk.infrastructure.security.User;

@With
@Value
@Builder
@EqualsAndHashCode(of = {"ownerId","name","nip"})
@ToString(of = {"ownerId", "name", "surname", "phoneNumber","nip","regon"})
public class Owner {
    Long ownerId;
    String name;
    String surname;
    String phoneNumber;
    String nip;
    String regon;
    User user;
}
