package pl.dlusk.business.dao;

import pl.dlusk.domain.Client;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

import java.util.List;

public interface ClientDAO {
    List<FoodOrder> findOrdersByClientId(Long clientId);

    Client save(Client client, FoodOrderingAppUser user); // Zapisuje lub aktualizuje klienta.

    Client findByUserId(Long id); // Znajduje klienta po ID usera.

    List<Client> findAll(); // Zwraca listę wszystkich klientów.

    void deleteById(Long id); // Usuwa klienta po ID.

    void deactivateAccount(Long userId);
    Client findClientByOrderId(Long orderId);
    Client findClientByUsername(String username);

}
