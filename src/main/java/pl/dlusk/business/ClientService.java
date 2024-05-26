package pl.dlusk.business;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.ClientOrderHistory;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.OrderItem;
import pl.dlusk.infrastructure.security.User;
import pl.dlusk.infrastructure.security.UserDAO;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ClientService {

    private final ClientDAO clientDAO;
    private final FoodOrderDAO foodOrderDAO;
    private final UserDAO userDAO;
    private final FoodOrderService foodOrderService;

    @Transactional
    public Client registerClient(Client client) {
        return clientDAO.save(client);
    }


    @Transactional
    public ClientOrderHistory getClientOrderHistory(String username) {
        Long clientId = userDAO.findIdByUsername(username);
        log.info("########## ClientService #### getClientOrderHistory #  START");
        log.info("########## ClientService #### getClientOrderHistory #  clientId {}", clientId);
        List<FoodOrder> foodOrders = clientDAO.findOrdersByClientId(clientId);

        List<ClientOrderHistory.FoodOrderRequest> foodOrderRequests = foodOrders.stream()
                .map(foodOrder -> {
                    Set<OrderItem> orderItems = foodOrderDAO.findOrderItemsByFoodOrderId(foodOrder.getFoodOrderId());
                    log.info("########## ClientService #### getClientOrderHistory #  foodOrderId:" + foodOrder.getFoodOrderId() + " orderItems: " + orderItems);
                    return foodOrderService.convertToFoodOrderRequest(foodOrder, orderItems);
                })
                .sorted((o1, o2) -> o2.getOrderTime().compareTo(o1.getOrderTime()))
                .collect(Collectors.toList());
        log.info("########## ClientService #### getClientOrderHistory #  foodOrderRequests: " + foodOrderRequests);

        return ClientOrderHistory.builder()
                .customerId(clientId)
                .customerFoodOrders(foodOrderRequests)
                .build();
    }


    public Client getClientByUsername(String username) {
        log.info("########## ClientService #### getClientByUsername #  START ");

        Long clientId = userDAO.findIdByUsername(username);
        log.info("########## ClientService #### getClientByUsername #  clientId: " + clientId);

        Client client = clientDAO.findByUserId(clientId);
        log.info("########## ClientService #### getClientByUsername #  client: " + client);
        return client;
    }

    public Client getClientById(Long clientId){

        Client client = clientDAO.findByClientId(clientId);
        User user = userDAO.findByClientId(client.getClientId());
        client.withUser(user);
        return client;
    }
    public List<Client> getAllClients(){
        List<Client> clients = new ArrayList<>();
        System.out.println(clients);
        List<Client> all = clientDAO.findAll();
        for (Client client : all) {
            User user = userDAO.findByClientId(client.getClientId());
            clients.add(client.withUser(user));
        }
        return clients;
    }




}
