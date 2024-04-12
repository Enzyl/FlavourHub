package pl.dlusk.api.controller;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import pl.dlusk.domain.Payment;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Controller
@AllArgsConstructor
public class PaymentController {

    @GetMapping("/enterBlikCode")
    public String showBlikCodeForm() {
        return "blikPaymentView";
    }

    @PostMapping("/orderPaymentSuccess")
    public String processBlikCode(@RequestParam("blikCode") String blikCode, HttpSession session) {
        log.info("########## PaymentController #### processBlikCode #  START");
        BigDecimal totalAmount = (BigDecimal) session.getAttribute("totalValue");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDateTime = LocalDateTime.now().format(formatter);

        LocalDateTime paymentTime = LocalDateTime.parse(formattedDateTime, formatter);

        Payment payment = Payment.builder()
                .paymentAmount(totalAmount)
                .paymentMethod("Blik")
                .paymentStatus("Zrealizowano")
                .paymentTime(paymentTime)
                .build();

        session.setAttribute("payment", payment);
        log.info("########## PaymentController #### processBlikCode #  FINISH");
        return "redirect:/processOrder";
    }
}

