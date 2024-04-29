package pl.dlusk.business;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.ClientDAO;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.business.dao.PaymentDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.repository.ClientRepository;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserDAO;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class ClientService {

    private final ClientDAO clientDAO;

    private final FoodOrderingAppUserDAO foodOrderingAppUserRepository;
    private final ClientRepository clientRepository;
    private final FoodOrderDAO foodOrderDAO;
    private final FoodOrderingAppUserDAO foodOrderingAppUserDAO;
    private final FoodOrderService foodOrderService;

    @Transactional
    public Client registerClient(Client client) {
        return clientDAO.save(client);
    }


    @Transactional
    public ClientOrderHistory getClientOrderHistory(String username) {
        Long clientId = foodOrderingAppUserDAO.findIdByUsername(username);
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

        Long clientId = foodOrderingAppUserRepository.findIdByUsername(username);
        log.info("########## ClientService #### getClientByUsername #  clientId: " + clientId);

        Client client = clientRepository.findByUserId(clientId);
        log.info("########## ClientService #### getClientByUsername #  client: " + client);
        return client;
    }





}
