package heis.berg.pega_paga.controller;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.dto.EnrollBiometricRequest;
import heis.berg.pega_paga.dto.SimpleMessageResponse;
import heis.berg.pega_paga.service.BiometricService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/biometrics")
public class BiometricController {

    private final BiometricService biometricService;

    public BiometricController(BiometricService biometricService) {
        this.biometricService = biometricService;
    }

    @PostMapping("/fingerprint")
    public SimpleMessageResponse enrollFingerprint(
            @AuthenticationPrincipal AppUser currentUser,
            @Valid @RequestBody EnrollBiometricRequest request
    ) {
        biometricService.enroll(currentUser, request.fingerprint());
        return new SimpleMessageResponse("Fingerprint enrolled successfully");
    }
}
