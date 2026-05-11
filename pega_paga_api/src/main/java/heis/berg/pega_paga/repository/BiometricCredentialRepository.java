package heis.berg.pega_paga.repository;

import heis.berg.pega_paga.domain.entity.BiometricCredential;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BiometricCredentialRepository extends JpaRepository<BiometricCredential, UUID> {

    Optional<BiometricCredential> findByFingerprintHash(String fingerprintHash);

    Optional<BiometricCredential> findByUserId(UUID userId);
}
