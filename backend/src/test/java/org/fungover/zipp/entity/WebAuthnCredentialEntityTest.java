package org.fungover.zipp.entity;


import org.junit.jupiter.api.Test;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class WebAuthnCredentialEntityTest {

    @Test
    void testLogicAndFormatting() {
        User user = new User();
        byte[] id = "test-id".getBytes();

        WebAuthnCredentialEntity entity = new WebAuthnCredentialEntity(
            id, user, new byte[]{1,2}, 100L, "usb,nfc", null, null, "My Phone"
        );

        assertNotNull(entity.getEncodedId());
        assertNotEquals("error", entity.getEncodedId());

        Set<AuthenticatorTransport> transports = entity.getTransportsAsSet();

        boolean hasUsb = transports.stream()
            .anyMatch(t -> t.getValue().equalsIgnoreCase("usb"));

        assertTrue(hasUsb, "Transports should contain usb");
        assertEquals(2, transports.size());

        String transportStr = WebAuthnCredentialEntity.transportsToString(Set.of(AuthenticatorTransport.BLE));
        assertEquals("ble", transportStr);

        WebAuthnCredentialEntity empty = new WebAuthnCredentialEntity();
        assertEquals("error", empty.getEncodedId());
    }
}
