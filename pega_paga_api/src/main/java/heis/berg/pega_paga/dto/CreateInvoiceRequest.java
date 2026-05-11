package heis.berg.pega_paga.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record CreateInvoiceRequest(
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String description,
        String customerPhoneNumber
) {
}
