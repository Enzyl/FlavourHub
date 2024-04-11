package pl.dlusk.business.dao;

import pl.dlusk.domain.Payment;

public interface PaymentDAO {
    Payment findById(Long paymentId);
    Payment save(Payment payment);
    Payment findByFoodOrderId(Long foodOrderId);
}
