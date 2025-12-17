package org.fungover.zipp.service;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.entity.WebAuthnCredentialEntity;
import org.fungover.zipp.repository.WebAuthnCredentialEntityRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WebAuthnService {

    private final WebAuthnCredentialEntityRepository credentialRepository;

    public WebAuthnService(WebAuthnCredentialEntityRepository credentialRepository) {
        this.credentialRepository = credentialRepository;
    }

    public List<WebAuthnCredentialEntity> getUserPasskeys(User user) {
        return credentialRepository.findAllByUserId(user.getId());
    }

    public void deletePasskey(byte[] credentialId, User currentuser) {
        WebAuthnCredentialEntity credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new IllegalArgumentException("Passkey not found"));

        if (!credential.getUser().getId().equals(currentuser.getId())) {
            throw new AccessDeniedException("You are not allowed to delete this passkey");
        }

        credentialRepository.delete(credential);
    }
}
