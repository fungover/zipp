package org.fungover.zipp.service;

import org.fungover.zipp.entity.User;
import org.fungover.zipp.entity.WebAuthnCredentialEntity;
import org.fungover.zipp.repository.WebAuthnCredentialEntityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebAuthnServiceTest {

    @Mock
    private WebAuthnCredentialEntityRepository repository;

    @InjectMocks
    private WebAuthnService service;

    @Test
    void testGetUserPasskeys() {
        User user = new User();
        UUID userId = UUID.randomUUID();
        user.setId(userId);

        WebAuthnCredentialEntity mockEntity = new WebAuthnCredentialEntity(
            new byte[]{1, 2, 3},
            user,
            new byte[]{4, 5, 6},
            0L,
            "usb",
            null,
            null,
            "My Device"
        );

        when(repository.findAllByUserId(userId)).thenReturn(List.of(mockEntity));

        List<WebAuthnCredentialEntity> result = service.getUserPasskeys(user);

        assertEquals(1, result.size());
        assertEquals("My Device", result.get(0).getLabel());
        verify(repository).findAllByUserId(userId);
    }

    @Test
    void testDeletePasskey() {

        User user = new User();
        user.setId(UUID.randomUUID());
        byte[] credentialId = new byte[]{1, 2, 3};

        WebAuthnCredentialEntity mockEntity = mock(WebAuthnCredentialEntity.class);
        when(mockEntity.getUser()).thenReturn(user);

        when(repository.findById(any())).thenReturn(Optional.of(mockEntity));

        service.deletePasskey(credentialId, user);

        verify(repository).delete(mockEntity);
    }
}
