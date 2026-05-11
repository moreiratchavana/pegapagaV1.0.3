package heis.berg.pega_paga.repository;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.enums.UserRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findByPhoneNumber(String phoneNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    List<AppUser> findAllByRole(UserRole role);
}
