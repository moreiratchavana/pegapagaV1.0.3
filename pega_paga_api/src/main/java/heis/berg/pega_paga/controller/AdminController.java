package heis.berg.pega_paga.controller;

import heis.berg.pega_paga.dto.AdminSummaryResponse;
import heis.berg.pega_paga.dto.CreateOperatorRequest;
import heis.berg.pega_paga.dto.CreatePointOfSaleRequest;
import heis.berg.pega_paga.dto.InvoiceResponse;
import heis.berg.pega_paga.dto.PointOfSaleResponse;
import heis.berg.pega_paga.dto.TransactionResponse;
import heis.berg.pega_paga.dto.UserResponse;
import heis.berg.pega_paga.dto.WalletOperationRequest;
import heis.berg.pega_paga.dto.WalletResponse;
import heis.berg.pega_paga.mapper.ApiMapper;
import heis.berg.pega_paga.repository.AppUserRepository;
import heis.berg.pega_paga.repository.InvoiceRepository;
import heis.berg.pega_paga.repository.PointOfSaleRepository;
import heis.berg.pega_paga.repository.WalletTransactionRepository;
import heis.berg.pega_paga.service.AdminService;
import heis.berg.pega_paga.service.PointOfSaleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final PointOfSaleService pointOfSaleService;
    private final AppUserRepository appUserRepository;
    private final PointOfSaleRepository pointOfSaleRepository;
    private final InvoiceRepository invoiceRepository;
    private final WalletTransactionRepository walletTransactionRepository;

    public AdminController(
            AdminService adminService,
            PointOfSaleService pointOfSaleService,
            AppUserRepository appUserRepository,
            PointOfSaleRepository pointOfSaleRepository,
            InvoiceRepository invoiceRepository,
            WalletTransactionRepository walletTransactionRepository
    ) {
        this.adminService = adminService;
        this.pointOfSaleService = pointOfSaleService;
        this.appUserRepository = appUserRepository;
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.invoiceRepository = invoiceRepository;
        this.walletTransactionRepository = walletTransactionRepository;
    }

    @GetMapping("/summary")
    public AdminSummaryResponse getSummary() {
        return adminService.getSummary();
    }

    @PostMapping("/operators")
    public UserResponse createOperator(@Valid @RequestBody CreateOperatorRequest request) {
        return ApiMapper.toUserResponse(pointOfSaleService.createOperatorWithPointOfSale(request));
    }

    @PostMapping("/points-of-sale")
    public PointOfSaleResponse createPointOfSale(@Valid @RequestBody CreatePointOfSaleRequest request) {
        return ApiMapper.toPointOfSaleResponse(pointOfSaleService.createPointOfSale(request));
    }

    @GetMapping("/users")
    public List<UserResponse> getUsers() {
        return appUserRepository.findAll().stream()
                .map(ApiMapper::toUserResponse)
                .toList();
    }

    @GetMapping("/points-of-sale")
    public List<PointOfSaleResponse> getPointsOfSale() {
        return pointOfSaleRepository.findAll().stream()
                .map(ApiMapper::toPointOfSaleResponse)
                .toList();
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> getInvoices() {
        return invoiceRepository.findAll().stream()
                .map(ApiMapper::toInvoiceResponse)
                .toList();
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactions() {
        return walletTransactionRepository.findAll().stream()
                .map(ApiMapper::toTransactionResponse)
                .toList();
    }

    @PostMapping("/wallets/{userId}/deposit")
    public WalletResponse adminDeposit(
            @PathVariable UUID userId,
            @Valid @RequestBody WalletOperationRequest request
    ) {
        return ApiMapper.toWalletResponse(adminService.adminDeposit(userId, request.amount(), request.description()));
    }

    @PostMapping("/wallets/{userId}/withdraw")
    public WalletResponse adminWithdraw(
            @PathVariable UUID userId,
            @Valid @RequestBody WalletOperationRequest request
    ) {
        return ApiMapper.toWalletResponse(adminService.adminWithdraw(userId, request.amount(), request.description()));
    }
}
