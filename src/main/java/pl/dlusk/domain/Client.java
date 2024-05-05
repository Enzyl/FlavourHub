package pl.dlusk.domain;

import lombok.*;
import pl.dlusk.infrastructure.security.User;

import java.util.Set;

@With
@Value
@Builder
@EqualsAndHashCode(of = "clientId")
@ToString(of = {"clientId", "fullName", "phoneNumber"})
public class Client {
    Long clientId;
    String fullName;
    String phoneNumber;
    User user;
    Set<FoodOrder> foodOrders;

}
