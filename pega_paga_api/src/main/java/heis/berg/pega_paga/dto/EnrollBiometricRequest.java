package heis.berg.pega_paga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EnrollBiometricRequest(
        @NotBlank @Size(min = 6, max = 512) String fingerprint
) {
}
