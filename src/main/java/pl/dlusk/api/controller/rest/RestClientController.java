package pl.dlusk.api.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import pl.dlusk.api.dto.ClientDTO;
import pl.dlusk.api.dto.ClientRegisterRequestDTO;
import pl.dlusk.api.dto.ClientsDTO;
import pl.dlusk.api.dto.mapper.ClientDTOMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
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

    @PostMapping("orders/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long orderId) {
        FoodOrder foodOrder = foodOrderService.getFoodOrderById(orderId);

        if (foodOrder == null) {
            return ResponseEntity.notFound().build(); // Return 404 Not Found if order not found
        }

        if (Duration.between(foodOrder.getOrderTime(), LocalDateTime.now()).toMinutes() <= 20) {
            foodOrderService.updateFoodOrderStatus(orderId, FoodOrderStatus.CANCELLED.toString());
            return ResponseEntity.ok().build(); // Return 200 OK on successful cancellation
        } else {
            return ResponseEntity.badRequest().build(); // Return 400 Bad Request if cancellation window has passed
        }
    }
    @GetMapping("/{clientId}/orders/history")
    public ResponseEntity<String> getClientOrderHistory(HttpSession session) {
        User user = (User) session.getAttribute("user");

        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // Unauthorized if no user found in session
        }

        String username = user.getUsername();
        ClientOrderHistory clientOrderHistory;
        try {
            clientOrderHistory = clientService.getClientOrderHistory(username);

            // Use ObjectMapper to convert ClientOrderHistory to JSON string
            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(clientOrderHistory);

            return ResponseEntity.ok(json); // Return 200 OK with JSON response body
        } catch (Exception e) {
            log.error("Error retrieving orders for user {}: {}", username, e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); // Internal server error on unexpected exceptions
        }
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
        URI location = URI.create("/api/v1/clients/" + registeredClient.getClientId()); // Fixed URI

        return ResponseEntity.created(location).body(registeredClient); // Correct response usage
    }




}
