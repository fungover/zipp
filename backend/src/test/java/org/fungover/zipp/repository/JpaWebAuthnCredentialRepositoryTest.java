package org.fungover.zipp.repository;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.entity.WebAuthnCredentialEntity;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.web.webauthn.api.Bytes;
import org.springframework.security.web.webauthn.api.CredentialRecord;
import org.springframework.security.web.webauthn.api.PublicKeyCose;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class JpaWebAuthnCredentialRepositoryTest {

    @Mock
    private WebAuthnCredentialEntityRepository credRepo;
    @Mock
    private UserRepository userRepo;
    @InjectMocks
    private JpaWebAuthnCredentialRepository repository;

    @Test
    void testSaveNewCredential() {

        byte[] idBytes = new byte[16];
        Bytes mockBytes = new Bytes(idBytes);
        PublicKeyCose mockKey = mock(PublicKeyCose.class);
        when(mockKey.getBytes()).thenReturn(new byte[]{1, 2, 3});

        CredentialRecord record = mock(CredentialRecord.class);
        when(record.getCredentialId()).thenReturn(mockBytes);
        when(record.getUserEntityUserId()).thenReturn(mockBytes);
        when(record.getPublicKey()).thenReturn(mockKey);

        User user = new User();
        user.setId(org.fungover.zipp.util.WebAuthnUuidUtils.bytesToUuid(idBytes));

        when(userRepo.findById(any())).thenReturn(Optional.of(user));
        when(credRepo.findById(any())).thenReturn(Optional.empty());

        repository.save(record);

        verify(credRepo).save(any(WebAuthnCredentialEntity.class));
    }

    @Test
    void testSaveExistingCredentialUpdatesCount() {

        byte[] idBytes = new byte[16];
        CredentialRecord record = mock(CredentialRecord.class);
        when(record.getCredentialId()).thenReturn(new Bytes(idBytes));
        when(record.getUserEntityUserId()).thenReturn(new Bytes(idBytes));
        when(record.getSignatureCount()).thenReturn(500L);

        WebAuthnCredentialEntity existing = mock(WebAuthnCredentialEntity.class);
        User user = new User();

        when(userRepo.findById(any())).thenReturn(Optional.of(user));
        when(credRepo.findById(any())).thenReturn(Optional.of(existing));

        repository.save(record);

        verify(existing).setSignatureCount(500L);
        verify(credRepo).save(existing);
    }

    @Test
    void testFindMethods() {
        byte[] id = new byte[]{1, 2, 3};
        Bytes bytesId = new Bytes(id);

        User user = new User();
        user.setId(UUID.randomUUID());

        WebAuthnCredentialEntity entity = mock(WebAuthnCredentialEntity.class);
        when(entity.getCredentialId()).thenReturn(id);
        when(entity.getPublicKey()).thenReturn(id);

        when(entity.getUser()).thenReturn(user);

        when(credRepo.findById(id)).thenReturn(Optional.of(entity));

        CredentialRecord result = repository.findByCredentialId(bytesId);

        assertNotNull(result);
    }

    @Test
    void testSaveWithMissingUserThrowsException() {
        CredentialRecord record = mock(CredentialRecord.class);
        when(record.getUserEntityUserId()).thenReturn(new Bytes(new byte[16]));

        when(userRepo.findById(any())).thenReturn(Optional.empty());

        assertThrows(IllegalStateException.class, () -> repository.save(record));
    }
}
