package pl.dlusk.infrastructure.security;

public interface FoodOrderingAppUserDAO {
    FoodOrderingAppUser findByUsername(String username);
    Long findIdByUsername(String username);
}
