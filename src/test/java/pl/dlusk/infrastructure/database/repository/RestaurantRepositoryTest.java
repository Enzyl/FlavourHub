package pl.dlusk.infrastructure.database.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import pl.dlusk.domain.*;
import pl.dlusk.infrastructure.database.entity.*;
import pl.dlusk.infrastructure.database.repository.jpa.*;
import pl.dlusk.infrastructure.database.repository.mapper.*;

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
    private RestaurantAddressEntityMapper restaurantAddressEntityMapper;
    @Mock
    private ReviewJpaRepository reviewJpaRepository;
    @Mock
    private MenuEntityMapper menuEntityMapper;
    @Mock
    private OwnerEntityMapper ownerEntityMapper;
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
        RestaurantAddress address = RestaurantAddress.builder()
                .restaurantAddressId(1L)
                .city("Test City")
                .postalCode("12345")
                .address("Test Address")
                .build();

        Owner owner = Owner.builder()
                .ownerId(1L)
                .build();

        Restaurant restaurantToSave = Restaurant.builder()
                .name("Test Restaurant")
                .address(address)
                .owner(owner)
                .build();

        when(restaurantAddressJpaRepository.findByCityPostalCodeAndAddress(address.getCity(), address.getPostalCode(), address.getAddress())).thenReturn(Optional.empty());
        when(restaurantEntityMapper.mapToEntity(any(Restaurant.class))).thenReturn(new RestaurantEntity());
        when(restaurantJpaRepository.save(any(RestaurantEntity.class))).thenReturn(new RestaurantEntity());
        when(restaurantEntityMapper.mapFromEntity(any(RestaurantEntity.class))).thenReturn(restaurantToSave);

        Restaurant result = restaurantRepository.addRestaurant(restaurantToSave, address, owner);

        assertThat(result).isNotNull();
        assertThat(result).isEqualToComparingFieldByField(restaurantToSave);
        verify(restaurantJpaRepository).save(any(RestaurantEntity.class));
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

        MenuEntity menuEntity = new MenuEntity();
        menuEntity.setId(1L);



        Menu expectedMenu = Menu.builder()
                .menuId(1L)
                .name("Test Menu")
                .description("Test description of the menu")

                .build();


        when(menuJpaRepository.findByRestaurantId(anyLong())).thenReturn(menuEntity);
        when(menuEntityMapper.mapFromEntity(menuEntity)).thenReturn(expectedMenu);


        Menu result = restaurantRepository.findMenuRestaurantById(1L);


        assertThat(result).isNotNull().isEqualToComparingFieldByField(expectedMenu);
        verify(menuJpaRepository).findByRestaurantId(1L);
    }


    @Test
    void getRestaurantsDeliveringToAreaShouldReturnRestaurants() {

        RestaurantEntity mockRestaurantEntity = new RestaurantEntity();
        mockRestaurantEntity.setId(1L);

        RestaurantDeliveryAreaEntity deliveryAreaEntity = new RestaurantDeliveryAreaEntity();
        deliveryAreaEntity.setRestaurantEntity(mockRestaurantEntity);


        Pageable pageable = PageRequest.of(0, 10);


        Page<RestaurantDeliveryAreaEntity> deliveryAreasPage = new PageImpl<>(List.of(deliveryAreaEntity), pageable, 1);


        when(restaurantDeliveryAreaJpaRepository.findByStreetName(eq("Test Street"), any(Pageable.class)))
                .thenReturn(deliveryAreasPage);

        List<RestaurantEntity> restaurantEntities = List.of(mockRestaurantEntity);
        when(restaurantJpaRepository.findAllById(anySet())).thenReturn(restaurantEntities);
        when(restaurantEntityMapper.mapFromEntity(any(RestaurantEntity.class))).thenReturn(restaurant);


        Page<Restaurant> result = restaurantRepository.findRestaurantsDeliveringToArea("Test Street", pageable);


        assertThat(result).isNotNull();
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0)).isEqualToComparingFieldByField(restaurant);
    }


    @Test
    void getReviewsByRestaurantIdShouldReturnReviews() {

        ReviewEntity reviewEntity = new ReviewEntity();
        reviewEntity.setId(1L);
        reviewEntity.setRating(5);
        reviewEntity.setComment("Excellent");
        reviewEntity.setReviewDate(LocalDateTime.now());


        List<ReviewEntity> reviewEntities = List.of(reviewEntity);


        Review expectedReview = Review.builder()
                .reviewId(1L)
                .rating(5)
                .comment("Excellent")
                .reviewDate(reviewEntity.getReviewDate())

                .build();


        when(reviewJpaRepository.findByRestaurantId(anyLong())).thenReturn(reviewEntities);
        when(reviewEntityMapper.mapFromEntity(reviewEntity)).thenReturn(expectedReview);


        List<Review> result = restaurantRepository.findReviewsByRestaurantId(1L);


        assertThat(result).hasSize(1);
        assertThat(result.get(0)).isEqualToComparingFieldByField(expectedReview);
        verify(reviewJpaRepository).findByRestaurantId(1L);
    }


}