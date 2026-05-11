package heis.berg.pega_paga.repository;

import heis.berg.pega_paga.domain.entity.PointOfSale;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointOfSaleRepository extends JpaRepository<PointOfSale, UUID> {

    Optional<PointOfSale> findByCode(String code);
}
