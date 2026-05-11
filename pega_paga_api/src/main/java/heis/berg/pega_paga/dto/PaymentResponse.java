package heis.berg.pega_paga.dto;

import java.math.BigDecimal;

public record PaymentResponse(
        String reference,
        String invoiceNumber,
        BigDecimal amount,
        String method,
        String payerPhoneNumber,
        String beneficiaryPhoneNumber,
        BigDecimal payerBalance,
        String status
) {
}
