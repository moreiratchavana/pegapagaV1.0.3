package heis.berg.pega_paga.config;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.enums.UserRole;
import heis.berg.pega_paga.repository.AppUserRepository;
import heis.berg.pega_paga.service.WalletService;
import heis.berg.pega_paga.util.PhoneNumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class BootstrapAdminConfig {

    @Bean
    public CommandLineRunner bootstrapSuperAdmin(
            AppUserRepository appUserRepository,
            WalletService walletService,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap.super-admin.name}") String superAdminName,
            @Value("${app.bootstrap.super-admin.phone}") String superAdminPhone,
            @Value("${app.bootstrap.super-admin.password}") String superAdminPassword
    ) {
        return args -> {
            String normalizedPhone = PhoneNumberUtils.normalize(superAdminPhone);
            if (appUserRepository.existsByPhoneNumber(normalizedPhone)) {
                return;
            }

            AppUser superAdmin = AppUser.builder()
                    .fullName(superAdminName.trim())
                    .phoneNumber(normalizedPhone)
                    .password(passwordEncoder.encode(superAdminPassword))
                    .role(UserRole.SUPER_ADMIN)
                    .build();

            AppUser savedSuperAdmin = appUserRepository.save(superAdmin);
            walletService.createWalletForUser(savedSuperAdmin);
        };
    }
}
