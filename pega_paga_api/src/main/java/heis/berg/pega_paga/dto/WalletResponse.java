package heis.berg.pega_paga.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record WalletResponse(
        UUID userId,
        String fullName,
        String phoneNumber,
        String accountNumber,
        BigDecimal balance
) {
}
