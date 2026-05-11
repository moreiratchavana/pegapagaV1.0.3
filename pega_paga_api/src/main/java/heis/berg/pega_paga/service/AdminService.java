package heis.berg.pega_paga.service;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.enums.UserRole;
import heis.berg.pega_paga.dto.AdminSummaryResponse;
import heis.berg.pega_paga.exception.ResourceNotFoundException;
import heis.berg.pega_paga.repository.AppUserRepository;
import heis.berg.pega_paga.repository.InvoiceRepository;
import heis.berg.pega_paga.repository.PointOfSaleRepository;
import heis.berg.pega_paga.repository.WalletTransactionRepository;
import java.math.BigDecimal;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminService {

    private final AppUserRepository appUserRepository;
    private final PointOfSaleRepository pointOfSaleRepository;
    private final InvoiceRepository invoiceRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletService walletService;

    public AdminService(
            AppUserRepository appUserRepository,
            PointOfSaleRepository pointOfSaleRepository,
            InvoiceRepository invoiceRepository,
            WalletTransactionRepository walletTransactionRepository,
            WalletService walletService
    ) {
        this.appUserRepository = appUserRepository;
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.invoiceRepository = invoiceRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.walletService = walletService;
    }

    @Transactional(readOnly = true)
    public AdminSummaryResponse getSummary() {
        return new AdminSummaryResponse(
                appUserRepository.count(),
                appUserRepository.findAllByRole(UserRole.CUSTOMER).size(),
                appUserRepository.findAllByRole(UserRole.POS_OPERATOR).size(),
                pointOfSaleRepository.count(),
                invoiceRepository.count(),
                walletTransactionRepository.count()
        );
    }

    @Transactional(readOnly = true)
    public AppUser getUserById(UUID userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    @Transactional
    public AppUser adminDeposit(UUID userId, BigDecimal amount, String description) {
        AppUser user = getUserById(userId);
        walletService.deposit(user, amount, description);
        user.setWalletAccount(walletService.getWalletForUser(user));
        return user;
    }

    @Transactional
    public AppUser adminWithdraw(UUID userId, BigDecimal amount, String description) {
        AppUser user = getUserById(userId);
        walletService.withdraw(user, amount, description);
        user.setWalletAccount(walletService.getWalletForUser(user));
        return user;
    }
}
