package heis.berg.pega_paga.repository;

import heis.berg.pega_paga.domain.entity.WalletTransaction;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction, UUID> {
}
