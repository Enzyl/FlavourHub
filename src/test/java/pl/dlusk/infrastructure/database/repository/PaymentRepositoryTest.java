package pl.dlusk.infrastructure.database.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.domain.Payment;
import pl.dlusk.infrastructure.database.entity.PaymentEntity;
import pl.dlusk.infrastructure.database.repository.jpa.PaymentJpaRepository;
import pl.dlusk.infrastructure.database.repository.mapper.PaymentEntityMapper;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
@ExtendWith(MockitoExtension.class)
class PaymentRepositoryTest {
    @Mock
    private PaymentEntityMapper paymentEntityMapper;

    @Mock
    private PaymentJpaRepository paymentJpaRepository;

    @InjectMocks
    private PaymentRepository paymentRepository;

    @BeforeEach
    void setUp() {
        // Set up any required configuration before each test
    }

    @Test
    void findByIdShouldReturnPayment() {
        // Given
        Long paymentId = 1L;
        PaymentEntity paymentEntity = new PaymentEntity();
        paymentEntity.setId(paymentId);
        Payment payment = Payment.builder()
                .paymentId(paymentId)
                .paymentMethod("Credit Card")
                .paymentStatus("Completed")
                .paymentTime(LocalDateTime.now())
                .paymentAmount(new BigDecimal("99.99"))
                .build();

        when(paymentJpaRepository.findById(paymentId)).thenReturn(Optional.of(paymentEntity));
        when(paymentEntityMapper.mapFromEntity(paymentEntity)).thenReturn(payment);

        // When
        Payment foundPayment = paymentRepository.findById(paymentId);

        // Then
        assertThat(foundPayment).isNotNull();
        assertThat(foundPayment.getPaymentId()).isEqualTo(paymentId);
        assertThat(foundPayment.getPaymentMethod()).isEqualTo(payment.getPaymentMethod());
        assertThat(foundPayment.getPaymentStatus()).isEqualTo(payment.getPaymentStatus());
        assertThat(foundPayment.getPaymentTime()).isEqualTo(payment.getPaymentTime());
        assertThat(foundPayment.getPaymentAmount()).isEqualTo(payment.getPaymentAmount());
    }

    @Test
    void saveShouldPersistPayment() {
        // Given
        Payment paymentToSave = Payment.builder()
                .paymentMethod("Credit Card")
                .paymentStatus("Pending")
                .paymentTime(LocalDateTime.now())
                .paymentAmount(new BigDecimal("49.99"))
                .build();
        PaymentEntity paymentEntityToSave = new PaymentEntity();
        PaymentEntity savedPaymentEntity = new PaymentEntity();
        savedPaymentEntity.setId(1L);
        Payment savedPayment = Payment.builder()
                .paymentId(1L)
                .paymentMethod("Credit Card")
                .paymentStatus("Pending")
                .paymentTime(LocalDateTime.now())
                .paymentAmount(new BigDecimal("49.99"))
                .build();

        when(paymentEntityMapper.mapToEntity(any(Payment.class))).thenReturn(paymentEntityToSave);
        when(paymentJpaRepository.save(paymentEntityToSave)).thenReturn(savedPaymentEntity);
        when(paymentEntityMapper.mapFromEntity(savedPaymentEntity)).thenReturn(savedPayment);

        // When
        Payment result = paymentRepository.save(paymentToSave);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getPaymentId()).isEqualTo(savedPayment.getPaymentId());
        assertThat(result.getPaymentMethod()).isEqualTo(savedPayment.getPaymentMethod());
        assertThat(result.getPaymentStatus()).isEqualTo(savedPayment.getPaymentStatus());
        assertThat(result.getPaymentTime()).isEqualTo(savedPayment.getPaymentTime());
        assertThat(result.getPaymentAmount()).isEqualTo(savedPayment.getPaymentAmount());
    }
}