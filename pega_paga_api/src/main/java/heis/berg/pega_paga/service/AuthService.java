package heis.berg.pega_paga.service;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.enums.UserRole;
import heis.berg.pega_paga.dto.AuthResponse;
import heis.berg.pega_paga.dto.CustomerRegistrationRequest;
import heis.berg.pega_paga.dto.LoginRequest;
import heis.berg.pega_paga.exception.ConflictException;
import heis.berg.pega_paga.mapper.ApiMapper;
import heis.berg.pega_paga.repository.AppUserRepository;
import heis.berg.pega_paga.security.JwtService;
import heis.berg.pega_paga.util.PhoneNumberUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final AppUserRepository appUserRepository;
    private final WalletService walletService;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(
            AppUserRepository appUserRepository,
            WalletService walletService,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService
    ) {
        this.appUserRepository = appUserRepository;
        this.walletService = walletService;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse registerCustomer(CustomerRegistrationRequest request) {
        String normalizedPhone = PhoneNumberUtils.normalize(request.phoneNumber());
        if (appUserRepository.existsByPhoneNumber(normalizedPhone)) {
            throw new ConflictException("Phone number already registered");
        }

        AppUser user = AppUser.builder()
                .fullName(request.fullName().trim())
                .phoneNumber(normalizedPhone)
                .password(passwordEncoder.encode(request.password()))
                .role(UserRole.CUSTOMER)
                .build();

        AppUser savedUser = appUserRepository.save(user);
        walletService.createWalletForUser(savedUser);

        String token = jwtService.generateToken(savedUser);
        return ApiMapper.toAuthResponse(savedUser, token);
    }

    @Transactional(readOnly = true)
    public AuthResponse authenticate(LoginRequest request) {
        String normalizedPhone = PhoneNumberUtils.normalize(request.phoneNumber());
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(normalizedPhone, request.password())
        );

        AppUser user = appUserRepository.findByPhoneNumber(normalizedPhone)
                .orElseThrow();
        user.setWalletAccount(walletService.getWalletForUser(user));
        String token = jwtService.generateToken(user);
        return ApiMapper.toAuthResponse(user, token);
    }
}
