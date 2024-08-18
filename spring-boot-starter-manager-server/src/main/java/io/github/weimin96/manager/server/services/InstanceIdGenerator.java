package io.github.weimin96.manager.server.services;

import io.github.weimin96.manager.server.domain.value.InstanceId;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 实例id生成器
 * @author panwm
 * @since 2024/8/3 10:26
 */
public class InstanceIdGenerator {

    private static final char[] HEX_CHARS = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e',
            'f' };

    public InstanceId generateId(Registration registration) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] bytes = digest.digest(registration.getHealthUrl().getBytes(StandardCharsets.UTF_8));
            return InstanceId.of(new String(encodeHex(bytes, 0, 12)));
        }
        catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private char[] encodeHex(byte[] bytes, int offset, int length) {
        char[] chars = new char[length];
        for (int i = 0; i < length; i = i + 2) {
            byte b = bytes[offset + (i / 2)];
            chars[i] = HEX_CHARS[(b >>> 0x4) & 0xf];
            chars[i + 1] = HEX_CHARS[b & 0xf];
        }
        return chars;
    }
}
