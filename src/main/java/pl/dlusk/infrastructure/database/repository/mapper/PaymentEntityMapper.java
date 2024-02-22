package pl.dlusk.infrastructure.database.repository.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import pl.dlusk.domain.Payment;
import pl.dlusk.infrastructure.database.entity.PaymentEntity;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PaymentEntityMapper {

    @Mapping(target = "foodOrder", ignore = true)
    @Mapping(source = "id", target = "paymentId")
    Payment mapFromEntity(PaymentEntity entity);
    @Mapping(source = "paymentId", target = "id")
    PaymentEntity mapToEntity(Payment payment);
}
