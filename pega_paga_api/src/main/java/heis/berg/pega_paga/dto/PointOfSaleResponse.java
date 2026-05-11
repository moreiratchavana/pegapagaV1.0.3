package heis.berg.pega_paga.dto;

import java.util.List;
import java.util.UUID;

public record PointOfSaleResponse(
        UUID id,
        String code,
        String name,
        String location,
        boolean active,
        List<String> operators
) {
}
