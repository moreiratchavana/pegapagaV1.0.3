package heis.berg.pega_paga.controller;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.dto.PayByBiometricRequest;
import heis.berg.pega_paga.dto.PaymentResponse;
import heis.berg.pega_paga.service.PaymentService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/qr/{qrToken}")
    public PaymentResponse payByQr(
            @AuthenticationPrincipal AppUser currentUser,
            @PathVariable String qrToken
    ) {
        return paymentService.payByQr(currentUser, qrToken);
    }

    @PostMapping("/biometric")
    public PaymentResponse payByBiometric(
            @AuthenticationPrincipal AppUser currentUser,
            @Valid @RequestBody PayByBiometricRequest request
    ) {
        return paymentService.payByBiometric(currentUser, request);
    }
}
