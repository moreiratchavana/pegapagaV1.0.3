package heis.berg.pega_paga.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String fullName,
        String phoneNumber,
        String role,
        String accountNumber,
        BigDecimal balance,
        String pointOfSaleCode
) {
}
