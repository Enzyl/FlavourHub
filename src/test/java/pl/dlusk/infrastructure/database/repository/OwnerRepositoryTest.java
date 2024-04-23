package pl.dlusk.infrastructure.database.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.domain.Owner;
import pl.dlusk.infrastructure.database.entity.OwnerEntity;
import pl.dlusk.infrastructure.database.repository.jpa.OwnerJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.OwnerEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUser;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntity;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserEntityMapper;
import pl.dlusk.infrastructure.security.FoodOrderingAppUserJpaRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OwnerRepositoryTest {

    @Mock
    private OwnerEntityMapper ownerEntityMapper;

    @Mock
    private OwnerJpaRepository ownerJpaRepository;
    @Mock
    private FoodOrderingAppUserJpaRepository foodOrderingAppUserJpaRepository;
    @Mock
    private FoodOrderingAppUserEntityMapper foodOrderingAppUserEntityMapper;

    @InjectMocks
    private OwnerRepository ownerRepository;

    private Owner owner;
    private OwnerEntity ownerEntity;

    @BeforeEach
    void setUp() {
        ownerEntity = new OwnerEntity();
        ownerEntity.setId(1L);
        ownerEntity.setName("John");
        ownerEntity.setSurname("Doe");
        ownerEntity.setPhoneNumber("123456789");
        ownerEntity.setNip("1234567890");
        ownerEntity.setRegon("12345678901234");

        owner = Owner.builder()
                .ownerId(1L)
                .name("John")
                .surname("Doe")
                .phoneNumber("123456789")
                .nip("1234567890")
                .regon("12345678901234")
                .build();
    }

    @Test
    void findByIdShouldReturnOwner() {
        when(ownerJpaRepository.findById(anyLong())).thenReturn(Optional.of(ownerEntity));
        when(ownerEntityMapper.mapFromEntity(any(OwnerEntity.class))).thenReturn(owner);

        Optional<Owner> foundOwner = ownerRepository.findById(1L);

        assertThat(foundOwner).isPresent();
        assertThat(foundOwner.get()).isEqualToComparingFieldByField(owner);
    }

    @Test
    void findAllShouldReturnAllOwners() {
        when(ownerJpaRepository.findAll()).thenReturn(List.of(ownerEntity));
        when(ownerEntityMapper.mapFromEntity(any(OwnerEntity.class))).thenReturn(owner);

        List<Owner> owners = ownerRepository.findAll();

        assertThat(owners).hasSize(1);
        assertThat(owners.get(0)).isEqualToComparingFieldByField(owner);
    }


    @Test
    void saveOwnerWithUserBeforeShouldPersistOwner() {
        FoodOrderingAppUser mockUser = mock(FoodOrderingAppUser.class);

        FoodOrderingAppUserEntity userEntity = new FoodOrderingAppUserEntity();
        userEntity.setUsername("testUsername");
        when(foodOrderingAppUserEntityMapper.mapToEntity(any(FoodOrderingAppUser.class))).thenReturn(userEntity);
        when(foodOrderingAppUserJpaRepository.save(any(FoodOrderingAppUserEntity.class))).thenReturn(userEntity);

        OwnerEntity ownerEntityWithUser = new OwnerEntity();
        ownerEntityWithUser.setUser(userEntity);

        when(ownerEntityMapper.mapToEntity(any(Owner.class))).thenReturn(ownerEntityWithUser);
        when(ownerJpaRepository.save(any(OwnerEntity.class))).thenReturn(ownerEntityWithUser);
        when(ownerEntityMapper.mapFromEntity(ownerEntityWithUser)).thenReturn(owner.withUser(mockUser));

        Owner savedOwner = ownerRepository.saveOwnerWithUserBefore(owner, mockUser);

        assertThat(savedOwner).isNotNull();

        assertThat(savedOwner).isEqualToIgnoringGivenFields(owner, "user");
    }



    @Test
    void saveOwnerShouldPersistOwner() {
        when(ownerEntityMapper.mapToEntity(any(Owner.class))).thenReturn(ownerEntity);
        when(ownerJpaRepository.save(any(OwnerEntity.class))).thenReturn(ownerEntity);
        when(ownerEntityMapper.mapFromEntity(any(OwnerEntity.class))).thenReturn(owner);

        Owner savedOwner = ownerRepository.saveOwner(owner);

        assertThat(savedOwner).isNotNull();
        assertThat(savedOwner).isEqualToComparingFieldByField(owner);
    }

    @Test
    void deleteByIdShouldRemoveOwner() {
        OwnerEntity mockOwnerEntity = new OwnerEntity();
        mockOwnerEntity.setId(1L);
        FoodOrderingAppUserEntity mockUserEntity = new FoodOrderingAppUserEntity();
        mockOwnerEntity.setUser(mockUserEntity);

        mockUserEntity.setId(1L);

        when(ownerJpaRepository.findById(1L)).thenReturn(Optional.of(mockOwnerEntity));
        doNothing().when(foodOrderingAppUserJpaRepository).deleteById(1L);
        doNothing().when(ownerJpaRepository).deleteById(1L);

        ownerRepository.deleteById(1L);

        verify(foodOrderingAppUserJpaRepository, times(1)).deleteById(1L);
        verify(ownerJpaRepository, times(1)).deleteById(1L);
    }


    @Test
    void findBySurnameShouldReturnOwners() {
        when(ownerJpaRepository.findBySurname(anyString())).thenReturn(List.of(ownerEntity));
        when(ownerEntityMapper.mapFromEntity(any(OwnerEntity.class))).thenReturn(owner);

        List<Owner> owners = ownerRepository.findBySurname("Doe");

        assertThat(owners).hasSize(1);
        assertThat(owners.get(0)).isEqualToComparingFieldByField(owner);
    }

    @Test
    void findByNipShouldReturnOwner() {
        when(ownerJpaRepository.findByNip(anyString())).thenReturn(Optional.of(ownerEntity));
        when(ownerEntityMapper.mapFromEntity(any(OwnerEntity.class))).thenReturn(owner);

        Optional<Owner> foundOwner = ownerRepository.findByNip("1234567890");

        assertThat(foundOwner).isPresent();
        assertThat(foundOwner.get()).isEqualToComparingFieldByField(owner);
    }

    @Test
    void findByPhoneNumberShouldReturnOwner() {
        when(ownerJpaRepository.findByPhoneNumber(anyString())).thenReturn(ownerEntity);
        when(ownerEntityMapper.mapFromEntity(any(OwnerEntity.class))).thenReturn(owner);

        Owner foundOwner = ownerRepository.findByPhoneNumber("123456789");

        assertThat(foundOwner).isNotNull();
        assertThat(foundOwner).isEqualToComparingFieldByField(owner);
    }

    @Test
    void existsByIdShouldReturnTrueWhenOwnerExists() {

        when(ownerJpaRepository.findById(anyLong())).thenReturn(Optional.of(ownerEntity));

        boolean exists = ownerRepository.existsById(1L);

        assertThat(exists).isTrue();
    }


    @Test
    void existsByNipShouldReturnTrueWhenOwnerExists() {
        when(ownerJpaRepository.findByNip(anyString())).thenReturn(Optional.of(ownerEntity));
        when(ownerEntityMapper.mapFromEntity(any(OwnerEntity.class))).thenReturn(owner);

        boolean exists = ownerRepository.existsByNip("1234567890");

        assertThat(exists).isTrue();
    }



}