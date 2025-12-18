package org.fungover.zipp.entity;

import org.junit.jupiter.api.Test;
import org.springframework.security.web.webauthn.api.AuthenticatorTransport;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class WebAuthnCredentialEntityTest {

    @Test
    void testLogicAndFormatting() {
        User user = new User();
        byte[] id = "test-id".getBytes();

        WebAuthnCredentialEntity entity = new WebAuthnCredentialEntity(id, user, new byte[]{1, 2}, 100L, "usb,nfc",
                null, null, "My Phone");

        assertNotNull(entity.getEncodedId());
        assertNotEquals("error", entity.getEncodedId());

        Set<AuthenticatorTransport> transports = entity.getTransportsAsSet();

        boolean hasUsb = transports.stream().anyMatch(t -> t.getValue().equalsIgnoreCase("usb"));

        assertTrue(hasUsb, "Transports should contain usb");
        assertEquals(2, transports.size());

        String transportStr = WebAuthnCredentialEntity.transportsToString(Set.of(AuthenticatorTransport.BLE));
        assertEquals("ble", transportStr);

        WebAuthnCredentialEntity empty = new WebAuthnCredentialEntity();
        assertEquals("error", empty.getEncodedId());
    }

    @Test
    void testAllGettersSettersAndCopyLogic() {
        User user = new User();
        byte[] id = new byte[]{1, 2, 3};
        WebAuthnCredentialEntity entity = new WebAuthnCredentialEntity(id, user, id, 1L, "usb", id, id, "Label");

        assertArrayEquals(id, entity.getCredentialId());
        assertEquals(user, entity.getUser());
        assertArrayEquals(id, entity.getPublicKey());
        assertEquals(1L, entity.getSignatureCount());
        assertEquals("usb", entity.getTransports());
        assertArrayEquals(id, entity.getAttestationObject());
        assertArrayEquals(id, entity.getClientDataJSON());
        assertEquals("Label", entity.getLabel());

        entity.setLabel("New");
        assertEquals("New", entity.getLabel());
        entity.setSignatureCount(2L);
        assertEquals(2L, entity.getSignatureCount());
        entity.setTransports("nfc");
        assertEquals("nfc", entity.getTransports());

        WebAuthnCredentialEntity nullEntity = new WebAuthnCredentialEntity(null, null, null, 0, null, null, null, null);
        assertNull(nullEntity.getCredentialId());
    }

    @Test
    void testEqualsAndHashCode() {
        byte[] id1 = new byte[]{1};
        byte[] id2 = new byte[]{2};
        WebAuthnCredentialEntity e1 = new WebAuthnCredentialEntity(id1, null, null, 0, null, null, null, null);
        WebAuthnCredentialEntity e1Duplicate = new WebAuthnCredentialEntity(id1, null, null, 0, null, null, null, null);
        WebAuthnCredentialEntity e2 = new WebAuthnCredentialEntity(id2, null, null, 0, null, null, null, null);

        assertEquals(e1, e1Duplicate);
        assertNotEquals(e1, e2);
        assertNotEquals(e1, null);
        assertNotEquals(e1, "not an entity");
        assertEquals(e1.hashCode(), e1Duplicate.hashCode());
    }
}
