package pl.dlusk.api.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.dlusk.api.dto.ClientDTO;
import pl.dlusk.api.dto.ClientOrderHistoryDTO;
import pl.dlusk.api.dto.ClientRegisterRequestDTO;
import pl.dlusk.api.dto.ClientsDTO;
import pl.dlusk.api.dto.mapper.ClientDTOMapper;
import pl.dlusk.api.dto.mapper.ClientOrderHistoryDTOMapper;
import pl.dlusk.business.ClientService;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.ClientOrderHistory;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.FoodOrderStatus;
import pl.dlusk.infrastructure.database.repository.ClientRepository;
import pl.dlusk.infrastructure.database.repository.jpa.ClientJpaRepository;
import pl.dlusk.infrastructure.security.User;

import java.net.URI;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/clients")
@AllArgsConstructor
public class RestClientController {

    private final ClientService clientService;
    private final ClientRepository clientRepository;
    private final ClientDTOMapper clientDTOMapper;
    private final FoodOrderService foodOrderService;
    private final ClientJpaRepository clientJpaRepository;
    private final ClientOrderHistoryDTOMapper clientOrderHistoryDTOMapper;
    @PostMapping("orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        FoodOrder foodOrder = foodOrderService.getFoodOrderById(orderId);

        if (foodOrder == null) {
            return ResponseEntity.notFound().build();
        }

        if (Duration.between(foodOrder.getOrderTime(), LocalDateTime.now()).toMinutes() <= 20) {
            foodOrderService.updateFoodOrderStatus(orderId, FoodOrderStatus.CANCELLED.toString());
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.badRequest().build();
        }
    }
    @GetMapping("/{username}/orders/history")
    public ResponseEntity<ClientOrderHistoryDTO> getClientOrderHistory(@PathVariable String username) {

        log.info("########## RestClientController #### getClientOrderHistory #  START ");


        Client client = clientService.getClientByUsername(username);
        if (client == null) {
            log.info("########## RestClientController #### getClientOrderHistory #  NIE ZNALEZIONO KLIENTA");
            return ResponseEntity.notFound().build();
        }


        ClientOrderHistory clientOrderHistory = clientService.getClientOrderHistory(username);
        log.info("########## RestClientController #### getClientOrderHistory #  clientOrderHistory {}", clientOrderHistory);
        if (clientOrderHistory.getCustomerFoodOrders().isEmpty()) {
            log.info("########## RestClientController #### getClientOrderHistory #  NIE ZNALEZIONO ZAMOWIEN");
            return ResponseEntity.noContent().build();
        }

        ClientOrderHistoryDTO clientOrderHistoryDTO = clientOrderHistoryDTOMapper.mapToDTO(clientOrderHistory);
        log.info("########## RestClientController ##### getClientOrderHistory ### clientOrderHistoryDTO: " + clientOrderHistoryDTO);
        return ResponseEntity.ok(clientOrderHistoryDTO);
    }

    @GetMapping("{clientId}")
    public ResponseEntity<ClientDTO> getClientDetails(@PathVariable Long clientId) {
        try {
            log.info("getClientDetails ### clientId {}", clientId);
            Client client = clientService.getClientById(clientId);
            ClientDTO clientDTO = clientDTOMapper.mapToDTO(client);
            return ResponseEntity.ok(clientDTO);
        } catch (EntityNotFoundException e) {
            log.error("Client not found with ID: {}", clientId);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            log.error("Failed to fetch client details for ID: {}", clientId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    @GetMapping
    public ClientsDTO clientList(){
        List<Client> allClients = clientService.getAllClients();
        List<ClientDTO> allClientsDTO = clientsToClientsDTO(allClients);
        ClientsDTO clientsDTO = ClientsDTO.of(allClientsDTO);
        return clientsDTO;
    }

    private List<ClientDTO> clientsToClientsDTO(List<Client> allClients) {
        List<ClientDTO> allClientsDTO = new ArrayList<>();
        for (Client allClient : allClients) {
            ClientDTO clientDTO = clientDTOMapper.mapToDTO(allClient);
            allClientsDTO.add(clientDTO);
        }
        return allClientsDTO;
    }


    @PostMapping
    public ResponseEntity<?> addClient(@RequestBody ClientRegisterRequestDTO clientDTO) {

        Client client = Client.builder()
                .fullName(clientDTO.getFullName())
                .phoneNumber(clientDTO.getPhoneNumber())
                .user(
                        User.builder()
                        .username(clientDTO.getUserDTO().getUsername())
                        .password(clientDTO.getUserDTO().getPassword())
                        .email(clientDTO.getUserDTO().getEmail())
                        .enabled(true)
                        .build()
                )
                .build();
        Client registeredClient = clientService.registerClient(client);
        log.info("KLIENT ZAREJESTROWANY POMYŚLNIE ZAJEBIIIŚCIE");
        URI location = URI.create("/api/v1/clients/" + registeredClient.getClientId());

        return ResponseEntity.created(location).body(registeredClient);
    }




}
