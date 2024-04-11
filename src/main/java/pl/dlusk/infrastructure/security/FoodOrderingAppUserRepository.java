package pl.dlusk.infrastructure.security;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;

import java.util.Optional;
@Repository
@AllArgsConstructor
public class FoodOrderingAppUserRepository implements FoodOrderingAppUserDAO {
    FoodOrderingAppUserJpaRepository foodOrderingAppUserJpaRepository;
    FoodOrderingAppUserEntityMapper foodOrderingAppUserEntityMapper;
    @Override
    public FoodOrderingAppUser findByUsername(String username) {
        Optional<FoodOrderingAppUserEntity> byUsername = foodOrderingAppUserJpaRepository.findByUsername(username);
        if (byUsername.isEmpty()){
            throw new UsernameNotFoundException("User ["+username+"] not found");
        }
        FoodOrderingAppUser foodOrderingAppUser = foodOrderingAppUserEntityMapper.mapFromEntity(byUsername.get());
        return foodOrderingAppUser;
    }

    @Override
    public Long findIdByUsername(String username) {
        Optional<FoodOrderingAppUserEntity> byUsername = foodOrderingAppUserJpaRepository.findByUsername(username);
        Long id = byUsername.get().getId();
        return id;
    }
}
