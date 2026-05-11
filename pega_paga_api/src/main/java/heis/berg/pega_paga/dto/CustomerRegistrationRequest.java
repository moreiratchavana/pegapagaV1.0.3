package heis.berg.pega_paga.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CustomerRegistrationRequest(
        @NotBlank String fullName,
        @NotBlank @Size(min = 8, max = 20) String phoneNumber,
        @NotBlank @Size(min = 6, max = 100) String password
) {
}
