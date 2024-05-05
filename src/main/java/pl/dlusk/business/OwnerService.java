package pl.dlusk.business;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.Client;
import pl.dlusk.domain.Owner;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.infrastructure.security.User;
import pl.dlusk.infrastructure.security.UserDAO;
import pl.dlusk.infrastructure.security.UserRepository;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@AllArgsConstructor
public class OwnerService {

    private final OwnerDAO ownerDAO;
    private final RestaurantDAO restaurantDAO;
    private final UserDAO userDAO;
    private final UserRepository foodOrderingAppUserRepository;

    public List<Owner> getAllOwners() {
        return ownerDAO.findAll();
    }

    public Owner getOwnerById(Long ownerId) {

        Owner owner = ownerDAO.findById(ownerId)
                .orElseThrow(() -> new RuntimeException("Owner not found with id: " + ownerId));
        User user = userDAO.findByOwnerId(ownerId);
        owner.withUser(user);
        return owner;
    }

    @Transactional
    public Owner registerOwner(Owner owner, User user) {
        log.info("########## OwnerService #### registerOwner START");
        return ownerDAO.saveOwnerWithUserBefore(owner, user);
    }

    @Transactional
    public Owner updateOwner(Long ownerId, Owner ownerDetails) {
        Optional<Owner> ownerById = ownerDAO.findById(ownerId);
        return ownerDAO.saveOwner(ownerDetails);
    }

    public void deleteOwner(Long ownerId) {
        ownerDAO.deleteById(ownerId);
    }

    public Restaurant getRestaurantByOwnerId(Long ownerId) {
        return restaurantDAO.getRestaurantByOwnerId(ownerId);
    }

    public boolean verifyOwnerData(Owner owner) {
        if (owner == null) {
            return false;
        }

        // Sprawdzenie poprawności NIP-u
        if (!isValidNip(owner.getNip())) {
            return false;
        }

        // Sprawdzenie poprawności numeru telefonu
        if (!isValidPhoneNumber(owner.getPhoneNumber())) {
            return false;
        }
        return true;
    }

    public Owner assignRestaurantToOwner(Long ownerId, Long restaurantId) {
        Restaurant restaurantById = restaurantDAO.findRestaurantById(restaurantId);
        Owner ownerById = ownerDAO.findById(ownerId).orElseThrow();

        restaurantById.withOwner(ownerById);
        return ownerById;
    }

    private boolean isValidNip(String nip) {
        // Implementacja walidacji NIP-u, np. sprawdzenie długości i cyfr
        return nip != null && nip.matches("\\d{10}");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Implementacja walidacji numeru telefonu, np. sprawdzenie formatu
        return phoneNumber != null && phoneNumber.matches("\\+?\\d{9,15}");
    }

    public Restaurant findRestaurantsByOwner(Owner owner) {
        Owner ownerFound = ownerDAO.findById(owner.getOwnerId())
                .orElseThrow(() -> new UsernameNotFoundException("Owner not found with id: " + owner.getOwnerId()));

        Restaurant restaurantsByOwnerId = restaurantDAO.getRestaurantByOwnerId(ownerFound.getOwnerId());

        return restaurantsByOwnerId;
    }


    public User getUserByUsername(String username) {
        User userByUsername = foodOrderingAppUserRepository.findByUsername(username);
        return userByUsername;
    }
public Owner  getByUsername (String username) {
    Owner owner = ownerDAO.findByUsername(username);
    return owner;
}

    public Owner getAuthenticatedOwner(HttpSession session) {
        User appUser = (User) session.getAttribute("user");
        Long userId = foodOrderingAppUserRepository.findIdByUsername(appUser.getUsername());
        return ownerDAO.findByUserId(userId);
    }

}
