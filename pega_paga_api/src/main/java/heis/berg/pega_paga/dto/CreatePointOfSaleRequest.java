package heis.berg.pega_paga.dto;

import jakarta.validation.constraints.NotBlank;

public record CreatePointOfSaleRequest(
        @NotBlank String name,
        @NotBlank String location
) {
}
