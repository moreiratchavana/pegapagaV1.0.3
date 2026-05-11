package heis.berg.pega_paga.service;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.entity.Invoice;
import heis.berg.pega_paga.domain.enums.InvoiceStatus;
import heis.berg.pega_paga.domain.enums.PaymentMethod;
import heis.berg.pega_paga.domain.enums.TransactionType;
import heis.berg.pega_paga.domain.enums.UserRole;
import heis.berg.pega_paga.dto.PayByBiometricRequest;
import heis.berg.pega_paga.dto.PaymentResponse;
import heis.berg.pega_paga.exception.ConflictException;
import heis.berg.pega_paga.repository.InvoiceRepository;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PaymentService {

    private final PointOfSaleService pointOfSaleService;
    private final WalletService walletService;
    private final BiometricService biometricService;
    private final InvoiceRepository invoiceRepository;

    public PaymentService(
            PointOfSaleService pointOfSaleService,
            WalletService walletService,
            BiometricService biometricService,
            InvoiceRepository invoiceRepository
    ) {
        this.pointOfSaleService = pointOfSaleService;
        this.walletService = walletService;
        this.biometricService = biometricService;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional
    public PaymentResponse payByQr(AppUser payer, String qrToken) {
        Invoice invoice = pointOfSaleService.findInvoiceByQrToken(qrToken);
        validateInvoiceForPayment(invoice, payer);

        WalletService.TransferResult transferResult = walletService.transfer(
                payer,
                invoice.getCreatedBy(),
                invoice.getAmount(),
                invoice,
                TransactionType.QR_PAYMENT_DEBIT,
                TransactionType.QR_PAYMENT_CREDIT,
                "QR payment for invoice " + invoice.getInvoiceNumber()
        );

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(PaymentMethod.QR_CODE);
        invoice.setPaidBy(payer);
        invoice.setPaidAt(Instant.now());
        invoiceRepository.save(invoice);

        return new PaymentResponse(
                transferResult.getReference(),
                invoice.getInvoiceNumber(),
                invoice.getAmount(),
                PaymentMethod.QR_CODE.name(),
                payer.getPhoneNumber(),
                invoice.getCreatedBy().getPhoneNumber(),
                transferResult.getPayerBalance(),
                invoice.getStatus().name()
        );
    }

    @Transactional
    public PaymentResponse payByBiometric(AppUser actor, PayByBiometricRequest request) {
        if (actor.getRole() != UserRole.POS_OPERATOR && actor.getRole() != UserRole.SUPER_ADMIN) {
            throw new ConflictException("Only POS operators or super admin can trigger biometric payment");
        }

        Invoice invoice = pointOfSaleService.findInvoiceByNumber(request.invoiceNumber());
        if (actor.getRole() == UserRole.POS_OPERATOR
                && actor.getAssignedPointOfSale() != null
                && !actor.getAssignedPointOfSale().getId().equals(invoice.getPointOfSale().getId())) {
            throw new ConflictException("Operator cannot process invoice from another point of sale");
        }

        AppUser payer = biometricService.findUserByFingerprint(request.fingerprint());
        validateInvoiceForPayment(invoice, payer);

        WalletService.TransferResult transferResult = walletService.transfer(
                payer,
                invoice.getCreatedBy(),
                invoice.getAmount(),
                invoice,
                TransactionType.BIOMETRIC_PAYMENT_DEBIT,
                TransactionType.BIOMETRIC_PAYMENT_CREDIT,
                "Biometric payment for invoice " + invoice.getInvoiceNumber()
        );

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaymentMethod(PaymentMethod.BIOMETRIC);
        invoice.setPaidBy(payer);
        invoice.setPaidAt(Instant.now());
        invoiceRepository.save(invoice);

        return new PaymentResponse(
                transferResult.getReference(),
                invoice.getInvoiceNumber(),
                invoice.getAmount(),
                PaymentMethod.BIOMETRIC.name(),
                payer.getPhoneNumber(),
                invoice.getCreatedBy().getPhoneNumber(),
                transferResult.getPayerBalance(),
                invoice.getStatus().name()
        );
    }

    private void validateInvoiceForPayment(Invoice invoice, AppUser payer) {
        if (invoice.getStatus() != InvoiceStatus.PENDING) {
            throw new ConflictException("Invoice has already been processed");
        }
        if (invoice.getCustomer() != null && !invoice.getCustomer().getId().equals(payer.getId())) {
            throw new ConflictException("Invoice is reserved for another customer");
        }
        if (payer.getRole() != UserRole.CUSTOMER && payer.getRole() != UserRole.SUPER_ADMIN) {
            throw new ConflictException("Only customer wallets can be used to pay invoices");
        }
    }
}
