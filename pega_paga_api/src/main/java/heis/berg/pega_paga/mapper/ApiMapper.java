package heis.berg.pega_paga.mapper;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.entity.Invoice;
import heis.berg.pega_paga.domain.entity.PointOfSale;
import heis.berg.pega_paga.domain.entity.WalletAccount;
import heis.berg.pega_paga.domain.entity.WalletTransaction;
import heis.berg.pega_paga.dto.AuthResponse;
import heis.berg.pega_paga.dto.InvoiceResponse;
import heis.berg.pega_paga.dto.PointOfSaleResponse;
import heis.berg.pega_paga.dto.TransactionResponse;
import heis.berg.pega_paga.dto.UserResponse;
import heis.berg.pega_paga.dto.WalletResponse;
import java.util.List;

public final class ApiMapper {

    private ApiMapper() {
    }

    public static AuthResponse toAuthResponse(AppUser user, String token) {
        WalletAccount wallet = user.getWalletAccount();
        String pointOfSaleCode = user.getAssignedPointOfSale() != null ? user.getAssignedPointOfSale().getCode() : null;
        return new AuthResponse(
                token,
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole().name(),
                wallet != null ? wallet.getAccountNumber() : null,
                wallet != null ? wallet.getBalance() : null,
                pointOfSaleCode
        );
    }

    public static WalletResponse toWalletResponse(AppUser user) {
        WalletAccount wallet = user.getWalletAccount();
        return new WalletResponse(
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                wallet.getAccountNumber(),
                wallet.getBalance()
        );
    }

    public static UserResponse toUserResponse(AppUser user) {
        String pointOfSaleCode = user.getAssignedPointOfSale() != null ? user.getAssignedPointOfSale().getCode() : null;
        return new UserResponse(
                user.getId(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole().name(),
                user.isBiometricEnabled(),
                pointOfSaleCode
        );
    }

    public static PointOfSaleResponse toPointOfSaleResponse(PointOfSale pointOfSale) {
        List<String> operators = pointOfSale.getOperators().stream()
                .map(AppUser::getPhoneNumber)
                .toList();

        return new PointOfSaleResponse(
                pointOfSale.getId(),
                pointOfSale.getCode(),
                pointOfSale.getName(),
                pointOfSale.getLocation(),
                pointOfSale.isActive(),
                operators
        );
    }

    public static InvoiceResponse toInvoiceResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getInvoiceNumber(),
                invoice.getQrToken(),
                "pega-paga://pay?qrToken=" + invoice.getQrToken(),
                invoice.getAmount(),
                invoice.getDescription(),
                invoice.getStatus().name(),
                invoice.getPaymentMethod() != null ? invoice.getPaymentMethod().name() : null,
                invoice.getPointOfSale().getCode(),
                invoice.getCreatedBy().getPhoneNumber(),
                invoice.getCustomer() != null ? invoice.getCustomer().getPhoneNumber() : null,
                invoice.getPaidBy() != null ? invoice.getPaidBy().getPhoneNumber() : null,
                invoice.getCreatedAt(),
                invoice.getPaidAt()
        );
    }

    public static TransactionResponse toTransactionResponse(WalletTransaction transaction) {
        return new TransactionResponse(
                transaction.getReference(),
                transaction.getType().name(),
                transaction.getStatus().name(),
                transaction.getAmount(),
                transaction.getBalanceBefore(),
                transaction.getBalanceAfter(),
                transaction.getDescription(),
                transaction.getCounterpartyLabel(),
                transaction.getWalletAccount().getAccountNumber(),
                transaction.getInvoice() != null ? transaction.getInvoice().getInvoiceNumber() : null,
                transaction.getCreatedAt()
        );
    }
}
