package pl.dlusk.api.controller.rest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import pl.dlusk.api.dto.ClientDTO;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import pl.dlusk.api.dto.mapper.ClientDTOMapper;
import pl.dlusk.business.ClientService;
import pl.dlusk.business.FoodOrderService;
import pl.dlusk.business.UserService;
import pl.dlusk.domain.Client;
import pl.dlusk.infrastructure.security.exception.UsernameAlreadyExistsException;
@Slf4j
@RestController
@RequestMapping("/api/clients")
@AllArgsConstructor
public class RestClientController {

    private final ClientService clientService;
    private final ClientDTOMapper clientDTOMapper;

    @PostMapping("/register")
    public ResponseEntity<?> registerClient(@RequestBody ClientDTO clientDTO) {
        try {
            log.info("Starting client registration");
            Client client = clientDTOMapper.mapFromDTO(clientDTO);
            Client registeredClient = clientService.registerClient(client);
            log.info("Client registration successful: {}", registeredClient);
            return ResponseEntity.ok(registeredClient);  // Returning 200 OK with the registered client in the body
        } catch (UsernameAlreadyExistsException e) {
            log.error("Registration failed: Username or email already exists", e);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username or email already exists.");  // 409 Conflict
        } catch (Exception e) {
            log.error("Registration failed for user: {}", clientDTO.getUserDTO().getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Registration failed.");  // 500 Internal Server Error
        }
    }
}
