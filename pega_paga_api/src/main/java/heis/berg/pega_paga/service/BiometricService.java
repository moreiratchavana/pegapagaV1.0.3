package heis.berg.pega_paga.service;

import heis.berg.pega_paga.domain.entity.AppUser;
import heis.berg.pega_paga.domain.entity.BiometricCredential;
import heis.berg.pega_paga.exception.ConflictException;
import heis.berg.pega_paga.exception.ResourceNotFoundException;
import heis.berg.pega_paga.repository.AppUserRepository;
import heis.berg.pega_paga.repository.BiometricCredentialRepository;
import heis.berg.pega_paga.util.FingerprintEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BiometricService {

    private final BiometricCredentialRepository biometricCredentialRepository;
    private final AppUserRepository appUserRepository;
    private final FingerprintEncoder fingerprintEncoder;

    public BiometricService(
            BiometricCredentialRepository biometricCredentialRepository,
            AppUserRepository appUserRepository,
            FingerprintEncoder fingerprintEncoder
    ) {
        this.biometricCredentialRepository = biometricCredentialRepository;
        this.appUserRepository = appUserRepository;
        this.fingerprintEncoder = fingerprintEncoder;
    }

    @Transactional
    public void enroll(AppUser user, String rawFingerprint) {
        String fingerprintHash = fingerprintEncoder.encode(rawFingerprint);
        biometricCredentialRepository.findByFingerprintHash(fingerprintHash)
                .filter(credential -> !credential.getUser().getId().equals(user.getId()))
                .ifPresent(credential -> {
                    throw new ConflictException("Fingerprint already linked to another user");
                });

        BiometricCredential credential = biometricCredentialRepository.findByUserId(user.getId())
                .orElse(BiometricCredential.builder().user(user).build());
        credential.setFingerprintHash(fingerprintHash);
        biometricCredentialRepository.save(credential);

        user.setBiometricEnabled(true);
        user.setBiometricCredential(credential);
        appUserRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AppUser findUserByFingerprint(String rawFingerprint) {
        String fingerprintHash = fingerprintEncoder.encode(rawFingerprint);
        return biometricCredentialRepository.findByFingerprintHash(fingerprintHash)
                .map(BiometricCredential::getUser)
                .orElseThrow(() -> new ResourceNotFoundException("Biometric fingerprint not found"));
    }
}
