package heis.berg.pega_paga.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String fullName,
        String phoneNumber,
        String role,
        boolean biometricEnabled,
        String pointOfSaleCode
) {
}
