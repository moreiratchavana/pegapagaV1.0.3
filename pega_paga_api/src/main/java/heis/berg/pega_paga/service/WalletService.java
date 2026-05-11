package heis.berg.pega_paga.service;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.entity.Invoice;
import heis.berg.pega_paga.domain.entity.WalletAccount;
import heis.berg.pega_paga.domain.entity.WalletTransaction;
import heis.berg.pega_paga.domain.enums.TransactionStatus;
import heis.berg.pega_paga.domain.enums.TransactionType;
import heis.berg.pega_paga.exception.InsufficientBalanceException;
import heis.berg.pega_paga.exception.ResourceNotFoundException;
import heis.berg.pega_paga.repository.WalletAccountRepository;
import heis.berg.pega_paga.repository.WalletTransactionRepository;
import heis.berg.pega_paga.util.ReferenceGenerator;
import java.math.BigDecimal;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletAccountRepository walletAccountRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final ReferenceGenerator referenceGenerator;

    public WalletService(
            WalletAccountRepository walletAccountRepository,
            WalletTransactionRepository walletTransactionRepository,
            ReferenceGenerator referenceGenerator
    ) {
        this.walletAccountRepository = walletAccountRepository;
        this.walletTransactionRepository = walletTransactionRepository;
        this.referenceGenerator = referenceGenerator;
    }

    @Transactional
    public WalletAccount createWalletForUser(AppUser user) {
        WalletAccount walletAccount = WalletAccount.builder()
                .owner(user)
                .accountNumber(referenceGenerator.accountNumber())
                .balance(BigDecimal.ZERO)
                .build();
        WalletAccount savedWallet = walletAccountRepository.save(walletAccount);
        user.setWalletAccount(savedWallet);
        return savedWallet;
    }

    @Transactional(readOnly = true)
    public WalletAccount getWalletForUser(AppUser user) {
        return walletAccountRepository.findByOwner(user)
                .orElseThrow(() -> new ResourceNotFoundException("Wallet account not found for user " + user.getPhoneNumber()));
    }

    @Transactional
    public WalletAccount deposit(AppUser user, BigDecimal amount, String description) {
        WalletAccount wallet = getWalletForUser(user);
        applyCredit(wallet, amount, TransactionType.DEPOSIT, safeDescription(description, "Wallet deposit"), null, "SELF");
        return wallet;
    }

    @Transactional
    public WalletAccount withdraw(AppUser user, BigDecimal amount, String description) {
        WalletAccount wallet = getWalletForUser(user);
        applyDebit(wallet, amount, TransactionType.WITHDRAWAL, safeDescription(description, "Wallet withdrawal"), null, "SELF");
        return wallet;
    }

    @Transactional
    public TransferResult transfer(
            AppUser payer,
            AppUser beneficiary,
            BigDecimal amount,
            Invoice invoice,
            TransactionType debitType,
            TransactionType creditType,
            String description
    ) {
        WalletAccount payerWallet = getWalletForUser(payer);
        WalletAccount beneficiaryWallet = getWalletForUser(beneficiary);

        String debitDescription = description + " - debit";
        String creditDescription = description + " - credit";

        WalletTransaction debitTransaction = applyDebit(
                payerWallet,
                amount,
                debitType,
                debitDescription,
                invoice,
                beneficiary.getPhoneNumber()
        );
        applyCredit(
                beneficiaryWallet,
                amount,
                creditType,
                creditDescription,
                invoice,
                payer.getPhoneNumber()
        );

        return new TransferResult(debitTransaction.getReference(), payerWallet.getBalance());
    }

    private WalletTransaction applyCredit(
            WalletAccount wallet,
            BigDecimal amount,
            TransactionType type,
            String description,
            Invoice invoice,
            String counterpartyLabel
    ) {
        validateAmount(amount);
        BigDecimal balanceBefore = wallet.getBalance();
        BigDecimal balanceAfter = balanceBefore.add(amount);
        wallet.setBalance(balanceAfter);
        walletAccountRepository.save(wallet);
        return walletTransactionRepository.save(buildTransaction(
                wallet,
                amount,
                balanceBefore,
                balanceAfter,
                type,
                description,
                invoice,
                counterpartyLabel
        ));
    }

    private WalletTransaction applyDebit(
            WalletAccount wallet,
            BigDecimal amount,
            TransactionType type,
            String description,
            Invoice invoice,
            String counterpartyLabel
    ) {
        validateAmount(amount);
        BigDecimal balanceBefore = wallet.getBalance();
        if (balanceBefore.compareTo(amount) < 0) {
            throw new InsufficientBalanceException("Insufficient wallet balance");
        }
        BigDecimal balanceAfter = balanceBefore.subtract(amount);
        wallet.setBalance(balanceAfter);
        walletAccountRepository.save(wallet);
        return walletTransactionRepository.save(buildTransaction(
                wallet,
                amount.negate(),
                balanceBefore,
                balanceAfter,
                type,
                description,
                invoice,
                counterpartyLabel
        ));
    }

    private WalletTransaction buildTransaction(
            WalletAccount wallet,
            BigDecimal signedAmount,
            BigDecimal balanceBefore,
            BigDecimal balanceAfter,
            TransactionType type,
            String description,
            Invoice invoice,
            String counterpartyLabel
    ) {
        return WalletTransaction.builder()
                .reference(referenceGenerator.transactionReference())
                .walletAccount(wallet)
                .amount(signedAmount)
                .balanceBefore(balanceBefore)
                .balanceAfter(balanceAfter)
                .type(type)
                .status(TransactionStatus.COMPLETED)
                .description(description)
                .invoice(invoice)
                .counterpartyLabel(counterpartyLabel)
                .build();
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    private String safeDescription(String description, String fallback) {
        return description == null || description.isBlank() ? fallback : description.trim();
    }

    @Getter
    public static class TransferResult {
        private final String reference;
        private final BigDecimal payerBalance;

        public TransferResult(String reference, BigDecimal payerBalance) {
            this.reference = reference;
            this.payerBalance = payerBalance;
        }
    }
}
