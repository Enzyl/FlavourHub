package pl.dlusk.infrastructure.database.repository.mapper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.dlusk.domain.Payment;
import pl.dlusk.infrastructure.database.entity.PaymentEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
@ExtendWith(SpringExtension.class)
public class PaymentEntityMapperTest {
    private final PaymentEntityMapper mapper = Mappers.getMapper(PaymentEntityMapper.class);

    @Test
    void shouldMapPaymentEntityToPayment() {
        // Given
        PaymentEntity entity = new PaymentEntity();
        entity.setId(1L);
        entity.setPaymentMethod("Credit Card");
        entity.setPaymentStatus("Completed");
        entity.setPaymentTime(LocalDateTime.now());
        entity.setPaymentAmount(new BigDecimal("100.00"));

        // When
        Payment payment = mapper.mapFromEntity(entity);

        // Then
        assertThat(payment).isNotNull();
        assertThat(payment.getPaymentId()).isEqualTo(entity.getId());
        assertThat(payment.getPaymentMethod()).isEqualTo(entity.getPaymentMethod());
        assertThat(payment.getPaymentStatus()).isEqualTo(entity.getPaymentStatus());
        assertThat(payment.getPaymentTime()).isEqualTo(entity.getPaymentTime());
        assertThat(payment.getPaymentAmount()).isEqualByComparingTo(entity.getPaymentAmount());
    }

    @Test
    void shouldMapPaymentToPaymentEntity() {
        // Given
        Payment payment = Payment.builder()
                .paymentId(1L)
                .paymentMethod("Credit Card")
                .paymentStatus("Completed")
                .paymentTime(LocalDateTime.now())
                .paymentAmount(new BigDecimal("100.00"))
                .build();

        // When
        PaymentEntity entity = mapper.mapToEntity(payment);

        // Then
        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(payment.getPaymentId());
        assertThat(entity.getPaymentMethod()).isEqualTo(payment.getPaymentMethod());
        assertThat(entity.getPaymentStatus()).isEqualTo(payment.getPaymentStatus());
        assertThat(entity.getPaymentTime()).isEqualTo(payment.getPaymentTime());
        assertThat(entity.getPaymentAmount()).isEqualByComparingTo(payment.getPaymentAmount());
    }

}