package heis.berg.pega_paga.service;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.entity.Invoice;
import heis.berg.pega_paga.domain.entity.PointOfSale;
import heis.berg.pega_paga.domain.enums.InvoiceStatus;
import heis.berg.pega_paga.domain.enums.UserRole;
import heis.berg.pega_paga.dto.CreateInvoiceRequest;
import heis.berg.pega_paga.dto.CreateOperatorRequest;
import heis.berg.pega_paga.dto.CreatePointOfSaleRequest;
import heis.berg.pega_paga.exception.ConflictException;
import heis.berg.pega_paga.exception.ResourceNotFoundException;
import heis.berg.pega_paga.repository.AppUserRepository;
import heis.berg.pega_paga.repository.InvoiceRepository;
import heis.berg.pega_paga.repository.PointOfSaleRepository;
import heis.berg.pega_paga.util.PhoneNumberUtils;
import heis.berg.pega_paga.util.ReferenceGenerator;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PointOfSaleService {

    private final PointOfSaleRepository pointOfSaleRepository;
    private final AppUserRepository appUserRepository;
    private final InvoiceRepository invoiceRepository;
    private final PasswordEncoder passwordEncoder;
    private final ReferenceGenerator referenceGenerator;
    private final WalletService walletService;

    public PointOfSaleService(
            PointOfSaleRepository pointOfSaleRepository,
            AppUserRepository appUserRepository,
            InvoiceRepository invoiceRepository,
            PasswordEncoder passwordEncoder,
            ReferenceGenerator referenceGenerator,
            WalletService walletService
    ) {
        this.pointOfSaleRepository = pointOfSaleRepository;
        this.appUserRepository = appUserRepository;
        this.invoiceRepository = invoiceRepository;
        this.passwordEncoder = passwordEncoder;
        this.referenceGenerator = referenceGenerator;
        this.walletService = walletService;
    }

    @Transactional
    public PointOfSale createPointOfSale(CreatePointOfSaleRequest request) {
        PointOfSale pointOfSale = PointOfSale.builder()
                .code(referenceGenerator.pointOfSaleCode())
                .name(request.name().trim())
                .location(request.location().trim())
                .build();
        return pointOfSaleRepository.save(pointOfSale);
    }

    @Transactional
    public AppUser createOperatorWithPointOfSale(CreateOperatorRequest request) {
        String normalizedPhone = PhoneNumberUtils.normalize(request.phoneNumber());
        if (appUserRepository.existsByPhoneNumber(normalizedPhone)) {
            throw new ConflictException("Phone number already registered");
        }

        PointOfSale pointOfSale = pointOfSaleRepository.save(PointOfSale.builder()
                .code(referenceGenerator.pointOfSaleCode())
                .name(request.pointOfSaleName().trim())
                .location(request.pointOfSaleLocation().trim())
                .build());

        AppUser operator = AppUser.builder()
                .fullName(request.fullName().trim())
                .phoneNumber(normalizedPhone)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.POS_OPERATOR)
                .assignedPointOfSale(pointOfSale)
                .build();

        AppUser savedOperator = appUserRepository.save(operator);
        walletService.createWalletForUser(savedOperator);
        return savedOperator;
    }

    @Transactional
    public Invoice createInvoice(AppUser operator, CreateInvoiceRequest request) {
        PointOfSale pointOfSale = operator.getAssignedPointOfSale();
        if (pointOfSale == null) {
            throw new ConflictException("Operator is not assigned to a point of sale");
        }

        AppUser customer = null;
        if (request.customerPhoneNumber() != null && !request.customerPhoneNumber().isBlank()) {
            String normalizedPhone = PhoneNumberUtils.normalize(request.customerPhoneNumber());
            customer = appUserRepository.findByPhoneNumber(normalizedPhone)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(referenceGenerator.invoiceNumber())
                .qrToken(referenceGenerator.qrToken())
                .amount(request.amount())
                .description(request.description().trim())
                .status(InvoiceStatus.PENDING)
                .pointOfSale(pointOfSale)
                .createdBy(operator)
                .customer(customer)
                .build();
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public Invoice findInvoiceByQrToken(String qrToken) {
        return invoiceRepository.findByQrToken(qrToken)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found for QR token"));
    }

    @Transactional(readOnly = true)
    public Invoice findInvoiceByNumber(String invoiceNumber) {
        return invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice not found"));
    }
}
