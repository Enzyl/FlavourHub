package pl.dlusk.business;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.OwnerDAO;
import pl.dlusk.business.dao.RestaurantDAO;
import pl.dlusk.domain.Owner;
import pl.dlusk.domain.Restaurant;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OwnerService {

    private final OwnerDAO ownerDAO;
    private final RestaurantDAO restaurantDAO;

    //Metoda do pobierania listy wszystkich właścicieli restauracji.
    public List<Owner> getAllOwners() {
        return ownerDAO.findAll();
    }

    // Metoda do pobierania szczegółów konkretnego właściciela na podstawie jego ID.
    public Optional<Owner> getOwnerById(Long ownerId) {
        return ownerDAO.findById(ownerId);
    }


    // Metoda do dodawania nowego właściciela do bazy danych.
    @Transactional
    public Owner registerOwner(Owner owner, FoodOrderingAppUser user) {
        return ownerDAO.saveOwnerWithUserBefore(owner, user);
    }

    // Metoda do aktualizacji danych istniejącego właściciela.
    @Transactional
    public Owner updateOwner(Long ownerId, Owner ownerDetails) {
        Optional<Owner> ownerById = ownerDAO.findById(ownerId);
        return ownerDAO.saveOwner(ownerDetails);
    }

    // Metoda do usuwania właściciela na podstawie jego ID.
    public void deleteOwner(Long ownerId) {
        ownerDAO.deleteById(ownerId);
    }

    // Metoda do pobierania listy restauracji, które należą do danego właściciela.
    public List<Restaurant> getRestaurantsByOwnerId(Long ownerId) {
        return restaurantDAO.getRestaurantsByOwnerId(ownerId);
    }


    // Metoda do weryfikacji i walidacji danych właściciela przed ich zapisaniem. Może to obejmować sprawdzanie
    // poprawności NIP, numeru telefonu, adresu e-mail itp.
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


        // Tutaj możesz dodać dodatkowe walidacje, jeśli są potrzebne

        // Jeśli wszystkie sprawdzenia przeszły pomyślnie
        return true;
    }

    // Metoda do przypisywania restauracji do właściciela, co może być przydatne podczas
    // tworzenia nowej restauracji lub aktualizacji istniejących danych.

    public Owner assignRestaurantToOwner(Long ownerId, Long restaurantId) {
        Restaurant restaurantById = restaurantDAO.findRestaurantById(restaurantId);
        Owner ownerById = ownerDAO.findById(ownerId).orElseThrow();

        restaurantById.withOwner(ownerById);
        return ownerById;
    }



    // Metody pomocnicze
    private boolean isValidNip(String nip) {
        // Implementacja walidacji NIP-u, np. sprawdzenie długości i cyfr
        return nip != null && nip.matches("\\d{10}");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        // Implementacja walidacji numeru telefonu, np. sprawdzenie formatu
        return phoneNumber != null && phoneNumber.matches("\\+?\\d{9,15}");
    }


}
