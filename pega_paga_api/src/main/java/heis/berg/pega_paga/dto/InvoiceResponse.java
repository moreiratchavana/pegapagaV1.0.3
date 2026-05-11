package heis.berg.pega_paga.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record InvoiceResponse(
        String invoiceNumber,
        String qrToken,
        String qrPayload,
        BigDecimal amount,
        String description,
        String status,
        String paymentMethod,
        String pointOfSaleCode,
        String createdByPhoneNumber,
        String customerPhoneNumber,
        String paidByPhoneNumber,
        Instant createdAt,
        Instant paidAt
) {
}
