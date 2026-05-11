package heis.berg.pega_paga.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionResponse(
        String reference,
        String type,
        String status,
        BigDecimal amount,
        BigDecimal balanceBefore,
        BigDecimal balanceAfter,
        String description,
        String counterpartyLabel,
        String accountNumber,
        String invoiceNumber,
        Instant createdAt
) {
}
