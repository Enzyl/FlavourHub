package pl.dlusk.business;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import pl.dlusk.business.dao.FoodOrderDAO;
import pl.dlusk.business.dao.PaymentDAO;
import pl.dlusk.domain.FoodOrder;
import pl.dlusk.domain.Payment;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class PaymentService {

    private final PaymentDAO paymentDAO;
    private final FoodOrderDAO foodOrderDAO;

    public Payment registerPayment(Payment payment) {
        return paymentDAO.save(payment);
    }

    public String refundPayment(Long paymentId) {
        Payment paymentById = paymentDAO.findById(paymentId);
        BigDecimal paymentAmountToRefund = paymentById.getPaymentAmount();

        return ("Payment has been refunded. Amount refunded: " + paymentAmountToRefund);
    }

    public Payment getPaymentDetails(Long paymentId) {
        return paymentDAO.findById(paymentId);
    }

    public Payment updatePaymentStatus(Long paymentId, String status) {
        Payment paymentWithUpdatedStatus = paymentDAO.findById(paymentId).withPaymentStatus(status);
        paymentDAO.save(paymentWithUpdatedStatus);
        return paymentWithUpdatedStatus;
    }

    public List<Payment> getPaymentsByClientId(Long clientId) {
        List<FoodOrder> byClientId = foodOrderDAO.findByClientId(clientId);
        List<Payment> paymentsForClientByHisId = new ArrayList<>();

        for (FoodOrder foodOrder : byClientId) {
            Payment paymentByClientId = paymentDAO.findById(foodOrder.getFoodOrderId());
            paymentsForClientByHisId.add(paymentByClientId);
        }
        return paymentsForClientByHisId;
    }


}
