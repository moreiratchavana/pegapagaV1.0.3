package heis.berg.pega_paga.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record WalletOperationRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        String description
) {
}
