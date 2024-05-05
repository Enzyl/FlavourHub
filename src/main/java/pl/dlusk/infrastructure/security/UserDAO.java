package pl.dlusk.infrastructure.security;

public interface UserDAO {
    User findByUsername(String username);
    Long findIdByUsername(String username);

    User findByClientId(Long clientId);

    User findByOwnerId(Long ownerId);
}
