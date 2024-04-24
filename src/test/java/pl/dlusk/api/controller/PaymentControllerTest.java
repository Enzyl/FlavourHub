package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.dlusk.domain.Payment;

import java.math.BigDecimal;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.internal.verification.VerificationModeFactory.times;
@ExtendWith(MockitoExtension.class)
class PaymentControllerTest {
    @Mock
    private HttpSession session;

    @InjectMocks
    private PaymentController paymentController;

    @Test
    void showBlikCodeForm_ReturnsCorrectView() {
        String viewName = paymentController.showBlikCodeForm();
        assertEquals("blikPaymentView", viewName, "View name should match the expected BLIK payment view.");
    }

    @Test
    void processBlikCode_SuccessfulPayment() {
        // Arrange
        String blikCode = "123456";
        BigDecimal totalAmount = new BigDecimal("100.00");
        when(session.getAttribute("totalValue")).thenReturn(totalAmount);

        // Act
        String viewName = paymentController.processBlikCode(blikCode, session);

        // Assert
        assertEquals("redirect:/processOrder", viewName, "Redirection should go to the process order page.");

        // Use a custom argument captor to capture the Payment object and verify it
        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(session).setAttribute(eq("payment"), paymentCaptor.capture());
        verify(session).getAttribute("totalValue");

        // Additional assertions to validate the Payment object if necessary
        Payment capturedPayment = paymentCaptor.getValue();
        assertNotNull(capturedPayment);
        assertEquals("Blik", capturedPayment.getPaymentMethod());
        assertEquals(totalAmount, capturedPayment.getPaymentAmount());
    }



}