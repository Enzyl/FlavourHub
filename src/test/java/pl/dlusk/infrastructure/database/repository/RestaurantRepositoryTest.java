package pl.dlusk.infrastructure.database.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.entity.*;
import pl.dlusk.infrastructure.database.repository.jpa.*;
import pl.dlusk.infrastructure.database.repository.mapper.MenuEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.RestaurantEntityMapper;
import pl.dlusk.infrastructure.database.repository.mapper.ReviewEntityMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RestaurantRepositoryTest {
    @Mock
    private RestaurantJpaRepository restaurantJpaRepository;

    @Mock
    private RestaurantEntityMapper restaurantEntityMapper;
    @Mock
    private MenuJpaRepository menuJpaRepository;
    @Mock
    private ReviewEntityMapper reviewEntityMapper;
    @Mock
    private ReviewJpaRepository reviewJpaRepository;
    @Mock
    private MenuEntityMapper menuEntityMapper;
    @Mock
    private RestaurantDeliveryAreaJpaRepository restaurantDeliveryAreaJpaRepository;

    @Mock
    private OwnerJpaRepository ownerJpaRepository;
    @Mock
    private RestaurantAddressJpaRepository restaurantAddressJpaRepository;

    @InjectMocks
    private RestaurantRepository restaurantRepository;

    private RestaurantEntity restaurantEntity;
    private Restaurant restaurant;

    @BeforeEach
    void setUp() {
        restaurantEntity = new RestaurantEntity();
        restaurantEntity.setId(1L);
        restaurantEntity.setName("Test Restaurant");
        restaurantEntity.setDescription("Test Description");

        restaurant = Restaurant.builder()
                .restaurantId(1L)
                .name("Test Restaurant")
                .description("Test Description")
                .build();
    }

    @Test
    void getRestaurantsByOwnerIdShouldReturnListOfRestaurants() {
//
    }

    @Test
    void findRestaurantByIdShouldReturnRestaurant() {
        when(restaurantJpaRepository.findById(anyLong())).thenReturn(Optional.of(restaurantEntity));
        when(restaurantEntityMapper.mapFromEntity(any(RestaurantEntity.class))).thenReturn(restaurant);

        Restaurant result = restaurantRepository.findRestaurantById(1L);

        assertThat(result).isEqualToComparingFieldByField(restaurant);
    }

    @Test
    void getAllRestaurantsShouldReturnAllRestaurants() {
        when(restaurantJpaRepository.findAll()).thenReturn(List.of(restaurantEntity));
        when(restaurantEntityMapper.mapFromEntity(any(RestaurantEntity.class))).thenReturn(restaurant);

        List<Restaurant> result = restaurantRepository.findAllRestaurants();

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualToComparingFieldByField(restaurant);
    }

    @Test
    void addRestaurantShouldPersistRestaurant() {
        // Przygotowanie danych wejściowych z wykorzystaniem wzorca Builder
        RestaurantAddress address = RestaurantAddress.builder()
                .restaurantAddressId(1L)
                .city("Test City")
                .postalCode("12345")
                .address("Test Address")
                .build();

        Owner owner = Owner.builder()
                .ownerId(1L)
                // Uzupełnij pozostałe pola właściciela zgodnie z potrzebami
                .build();

        // Utwórz prawidłowy obiekt Restaurant do użycia w teście
        Restaurant restaurantToSave = Restaurant.builder()
                .name("Test Restaurant")
                .address(address)
                .owner(owner)
                // Uzupełnij pozostałe pola zgodnie z potrzebami
                .build();

        // Konfiguracja zachowania mocków
        when(ownerJpaRepository.findById(anyLong())).thenReturn(Optional.of(new OwnerEntity()));
        when(restaurantAddressJpaRepository.findById(anyLong())).thenReturn(Optional.of(new RestaurantAddressEntity()));

        // Upewnij się, że mapToEntity zwraca prawidłowy obiekt RestaurantEntity, a nie null
        when(restaurantEntityMapper.mapToEntity(restaurantToSave)).thenReturn(restaurantEntity);

        // Upewnij się, że zwracany obiekt RestaurantEntity nie jest null
        when(restaurantJpaRepository.save(restaurantEntity)).thenReturn(restaurantEntity);

        // Upewnij się, że mapFromEntity zwraca poprawnie zainicjowany obiekt Restaurant, a nie null
        when(restaurantEntityMapper.mapFromEntity(restaurantEntity)).thenReturn(restaurantToSave);

        // Wywołanie metody testowanej
        Restaurant result = restaurantRepository.addRestaurant(restaurantToSave, address, owner);

        // Weryfikacja wyników
        assertThat(result).isNotNull();  // Upewnij się, że result nie jest null
        assertThat(result).isEqualToComparingFieldByField(restaurantToSave);
        verify(restaurantJpaRepository).save(restaurantEntity);
    }



    @Test
    void updateRestaurantShouldUpdateDetails() {
        Restaurant updatedDetails = Restaurant.builder()
                .name("Updated Name")
                .description("Updated Description")
                .build();

        when(restaurantJpaRepository.findById(anyLong())).thenReturn(Optional.of(restaurantEntity));
        when(restaurantEntityMapper.mapFromEntity(any(RestaurantEntity.class))).thenReturn(updatedDetails);
        when(restaurantJpaRepository.save(any(RestaurantEntity.class))).thenReturn(restaurantEntity);

        Restaurant result = restaurantRepository.updateRestaurant(1L, updatedDetails);

        assertThat(result.getName()).isEqualTo(updatedDetails.getName());
        assertThat(result.getDescription()).isEqualTo(updatedDetails.getDescription());
        verify(restaurantJpaRepository).save(restaurantEntity);
    }

    @Test
    void deleteShouldRemoveRestaurant() {
        doNothing().when(restaurantJpaRepository).deleteById(anyLong());

        restaurantRepository.delete(1L);

        verify(restaurantJpaRepository).deleteById(1L);
    }

    @Test
    void getMenuRestaurantByIdShouldReturnMenu() {
        // Inicjalizacja MenuEntity, załóżmy, że jest ona odpowiednio skonfigurowana
        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setId(1L);
        // Ustawienie dodatkowych pól menuEntity w razie potrzeby

        // Przygotowanie oczekiwanego obiektu Menu z wykorzystaniem wzorca Builder
        Menu expectedMenu = Menu.builder()
                .menuId(1L)
                .name("Test Menu")
                .description("Test description of the menu")
                // Ustawienie dodatkowych pól Menu w razie potrzeby, np. restaurant, menuItems
                .build();

        // Konfiguracja zachowania mocków
        when(menuJpaRepository.findByRestaurantId(anyLong())).thenReturn(menuEntity);
        when(menuEntityMapper.mapFromEntity(menuEntity)).thenReturn(expectedMenu);

        // Wywołanie metody testowanej
        Menu result = restaurantRepository.findMenuRestaurantById(1L);

        // Weryfikacja wyników
        assertThat(result).isNotNull().isEqualToComparingFieldByField(expectedMenu);
        verify(menuJpaRepository).findByRestaurantId(1L);
    }


    @Test
    void getRestaurantsDeliveringToAreaShouldReturnRestaurants() {
        // Utworzenie mocka RestaurantEntity do użycia w RestaurantDeliveryAreaEntity
        RestaurantEntity mockRestaurantEntity = new RestaurantEntity();
        mockRestaurantEntity.setId(1L); // Ustawienie ID dla mocka RestaurantEntity
        // ... Ustaw pozostałe wymagane pola dla RestaurantEntity

        // Utworzenie RestaurantDeliveryAreaEntity z mockRestaurantEntity
        RestaurantDeliveryAreaEntity deliveryAreaEntity = new RestaurantDeliveryAreaEntity();
        deliveryAreaEntity.setRestaurantEntity(mockRestaurantEntity); // Ustawienie RestaurantEntity

        List<RestaurantEntity> restaurantEntities = List.of(restaurantEntity);
        when(restaurantDeliveryAreaJpaRepository.findByStreetName(anyString())).thenReturn(List.of(deliveryAreaEntity)); // Używanie zainicjalizowanego deliveryAreaEntity
        when(restaurantJpaRepository.findAllById(anySet())).thenReturn(restaurantEntities);
        when(restaurantEntityMapper.mapFromEntity(any(RestaurantEntity.class))).thenReturn(restaurant);

        List<Restaurant> result = restaurantRepository.findRestaurantsDeliveringToArea("Test Street");

        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualToComparingFieldByField(restaurant);
    }


    @Test
    void getReviewsByRestaurantIdShouldReturnReviews() {
        // Przygotowanie listy encji ReviewEntity, załóżmy, że są one odpowiednio skonfigurowane
        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(1L);
        reviewEntity.setRating(5);
        reviewEntity.setComment("Excellent");
        reviewEntity.setReviewDate(LocalDateTime.now());
        // Ustawienie dodatkowych pól reviewEntity w razie potrzeby, np. foodOrderEntity

        List<ReviewEntity> reviewEntities = List.of(reviewEntity);

        // Przygotowanie oczekiwanego obiektu Review z wykorzystaniem wzorca Builder
        Review expectedReview = Review.builder()
                .reviewId(1L)
                .rating(5)
                .comment("Excellent")
                .reviewDate(reviewEntity.getReviewDate())
                // Ustawienie dodatkowych pól Review w razie potrzeby, np. foodOrder
                .build();

        // Konfiguracja zachowania mocków
        when(reviewJpaRepository.findByRestaurantId(anyLong())).thenReturn(reviewEntities);
        when(reviewEntityMapper.mapFromEntity(reviewEntity)).thenReturn(expectedReview);

        // Wywołanie metody testowanej
        List<Review> result = restaurantRepository.findReviewsByRestaurantId(1L);

        // Weryfikacja wyników
        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualToComparingFieldByField(expectedReview);
        verify(reviewJpaRepository).findByRestaurantId(1L);
    }


}