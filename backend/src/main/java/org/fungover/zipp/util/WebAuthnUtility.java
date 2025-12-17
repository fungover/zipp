package org.fungover.zipp.util;

import java.util.Base64;

public class WebAuthnUtility {

    public static byte[] base64UrlToBytes(String base64Url) {
        return Base64.getUrlDecoder().decode(base64Url);
    }

    public static String bytesToBase64Url(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
