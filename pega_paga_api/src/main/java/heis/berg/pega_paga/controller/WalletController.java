package heis.berg.pega_paga.controller;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.dto.WalletOperationRequest;
import heis.berg.pega_paga.dto.WalletResponse;
import heis.berg.pega_paga.mapper.ApiMapper;
import heis.berg.pega_paga.service.WalletService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping("/me")
    public WalletResponse getMyWallet(@AuthenticationPrincipal AppUser currentUser) {
        currentUser.setWalletAccount(walletService.getWalletForUser(currentUser));
        return ApiMapper.toWalletResponse(currentUser);
    }

    @PostMapping("/deposit")
    public WalletResponse deposit(
            @AuthenticationPrincipal AppUser currentUser,
            @Valid @RequestBody WalletOperationRequest request
    ) {
        currentUser.setWalletAccount(walletService.deposit(currentUser, request.amount(), request.description()));
        return ApiMapper.toWalletResponse(currentUser);
    }

    @PostMapping("/withdraw")
    public WalletResponse withdraw(
            @AuthenticationPrincipal AppUser currentUser,
            @Valid @RequestBody WalletOperationRequest request
    ) {
        currentUser.setWalletAccount(walletService.withdraw(currentUser, request.amount(), request.description()));
        return ApiMapper.toWalletResponse(currentUser);
    }
}
