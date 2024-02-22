package pl.dlusk.business;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.ClientOrderHistory;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserJpaRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ClientService {

    private final ClientDAO clientDAO;

    private final FoodOrderingAppUserJpaRepository foodOrderingAppUserJpaRepository;


    @Transactional
    public Client registerClient(Client client, FoodOrderingAppUser user){
        return clientDAO.save(client,user);
    }

    @Transactional
    public void deactivateAccount(Client client){
        clientDAO.deactivateAccount(client.getUser().getUserId());
    }

    @Transactional
    public ClientOrderHistory getClientOrderHistory(Long clientId) {
        // Pobierz wszystkie zamówienia klienta
        List<FoodOrder> foodOrders = clientDAO.findOrdersByClientId(clientId);

        // Mapuj każde zamówienie na FoodOrderRequest
        List<ClientOrderHistory.FoodOrderRequest> foodOrderRequests = foodOrders.stream()
                .map(this::convertToFoodOrderRequest)
                .collect(Collectors.toList());

        // Stwórz i zwróć obiekt ClientOrderHistory
        return ClientOrderHistory.builder()
                .customerId(clientId)
                .customerFoodOrders(foodOrderRequests)
                .build();
    }

    private ClientOrderHistory.FoodOrderRequest convertToFoodOrderRequest(FoodOrder foodOrder) {
        // Przykład mapowania, zakładając że masz odpowiednie metody konwersji
        // oraz obiekty DTO dla Restaurant, OrderItem i Payment
        return ClientOrderHistory.FoodOrderRequest.builder()
                .orderTime(foodOrder.getOrderTime())
                .foodOrderStatus(foodOrder.getFoodOrderStatus())
                .totalPrice(foodOrder.getTotalPrice())
                .restaurant(foodOrder.getRestaurant())
                .orderItems(foodOrder.getOrderItems())
                .payment(foodOrder.getPayment())
                .build();
    }


}
