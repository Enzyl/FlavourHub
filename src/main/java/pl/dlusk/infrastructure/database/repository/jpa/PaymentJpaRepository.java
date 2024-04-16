package pl.dlusk.infrastructure.database.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pl.dlusk.infrastructure.database.entity.PaymentEntity;
@Repository

public interface PaymentJpaRepository extends JpaRepository<PaymentEntity, Long> {
    PaymentEntity findPaymentByFoodOrderEntity_Id(Long orderId);
}