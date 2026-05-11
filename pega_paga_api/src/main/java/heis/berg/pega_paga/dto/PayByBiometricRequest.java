package heis.berg.pega_paga.dto;

import jakarta.validation.constraints.NotBlank;

public record PayByBiometricRequest(
        @NotBlank String invoiceNumber,
        @NotBlank String fingerprint
) {
}
