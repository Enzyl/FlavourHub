package pl.dlusk.api.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.api.dto.*;
import pl.dlusk.domain.*;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ClientOrderHistoryDTOMapper {
    @Mapping(target = "customerFoodOrders", source = "customerFoodOrders")
    ClientOrderHistoryDTO mapToDTO(ClientOrderHistory history);

    @Mapping(target = "restaurant", source = "restaurant", qualifiedByName = "mapRestaurantToDTO")
    @Mapping(target = "orderItems", source = "orderItems", qualifiedByName = "mapOrderItemsToDTO")
    @Mapping(target = "payment", source = "payment", qualifiedByName = "mapPaymentToDTO")
    ClientOrderHistoryDTO.FoodOrderRequestDTO mapFoodOrderRequestToDTO(ClientOrderHistory.FoodOrderRequest request);

    @Named("mapRestaurantToDTO")
    RestaurantDTO mapRestaurantToDTO(Restaurant restaurant);

    @Named("mapOrderItemsToDTO")
    Set<OrderItemDTO> mapOrderItemsToDTO(Set<OrderItem> orderItems);

    @Named("mapPaymentToDTO")
    PaymentDTO mapPaymentToDTO(Payment payment);

    OrderItemDTO mapOrderItemToDTO(OrderItem orderItem);

    // Nowe mapowanie dla FoodOrder
    FoodOrderDTO mapFoodOrderToDTO(FoodOrder foodOrder);
}


