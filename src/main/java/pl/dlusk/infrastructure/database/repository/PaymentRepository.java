package pl.dlusk.infrastructure.database.repository;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;
import pl.dlusk.business.dao.PaymentDAO;
import pl.dlusk.domain.Payment;
import pl.dlusk.infrastructure.database.entity.PaymentEntity;
import pl.dlusk.infrastructure.database.repository.jpa.PaymentJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.PaymentEntityMapper;
@Repository
@AllArgsConstructor
public class PaymentRepository implements PaymentDAO {

    private final PaymentEntityMapper paymentEntityMapper;
    private final PaymentJpaRepository paymentJpaRepository;


    @Override
    public Payment findById(Long paymentId) {
        PaymentEntity paymentEntity = paymentJpaRepository.findById(paymentId).orElseThrow();
        return paymentEntityMapper.mapFromEntity(paymentEntity);
    }

    @Override
    public Payment save(Payment payment) {
        PaymentEntity paymentEntity = paymentEntityMapper.mapToEntity(payment);
        PaymentEntity savedPaymentEntity = paymentJpaRepository.save(paymentEntity);
        return paymentEntityMapper.mapFromEntity(savedPaymentEntity);
    }

    @Override
    public Payment findByFoodOrderId(Long foodOrderId) {
        PaymentEntity paymentByFoodOrderEntityId = paymentJpaRepository.findPaymentByFoodOrderEntity_Id(foodOrderId);
        return paymentEntityMapper.mapFromEntity(paymentByFoodOrderEntityId);
    }
}
