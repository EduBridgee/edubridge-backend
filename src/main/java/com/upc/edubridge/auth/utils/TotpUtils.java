package com.upc.edubridge.auth.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;

public class TotpUtils {
    public static boolean verify(String code, String secret) {
        if (code == null || secret == null) return false;
        try {
            long currentInterval = System.currentTimeMillis() / 1000 / 30;
            byte[] keyBytes = Base32.decode(secret);
            for (int i = -1; i <= 1; i++) {
                if (generateTOTP(keyBytes, currentInterval + i).equals(code)) {
                    return true;
                }
            }
        } catch (Exception e) {
            System.err.println("Error verifying TOTP: " + e.getMessage());
        }
        return false;
    }

    private static String generateTOTP(byte[] key, long time) throws GeneralSecurityException {
        byte[] data = ByteBuffer.allocate(8).putLong(time).array();
        SecretKeySpec signKey = new SecretKeySpec(key, "HmacSHA1");
        Mac mac = Mac.getInstance("HmacSHA1");
        mac.init(signKey);
        byte[] hash = mac.doFinal(data);

        int offset = hash[hash.length - 1] & 0xF;
        long truncatedHash = 0;
        for (int i = 0; i < 4; ++i) {
            truncatedHash <<= 8;
            truncatedHash |= (hash[offset + i] & 0xFF);
        }
        truncatedHash &= 0x7FFFFFFF;
        truncatedHash %= 1000000;

        return String.format("%06d", truncatedHash);
    }
}
