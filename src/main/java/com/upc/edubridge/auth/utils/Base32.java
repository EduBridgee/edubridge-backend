package com.upc.edubridge.auth.utils;

public class Base32 {
    private static final String ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";
    private static final int[] CHAR_MAP = new int[128];
    static {
        java.util.Arrays.fill(CHAR_MAP, -1);
        for (int i = 0; i < ALPHABET.length(); i++) {
            CHAR_MAP[ALPHABET.charAt(i)] = i;
            CHAR_MAP[Character.toLowerCase(ALPHABET.charAt(i))] = i;
        }
    }

    public static byte[] decode(String base32) {
        base32 = base32.trim().replaceAll("[\\s-]", "");
        int len = base32.length();
        while (len > 0 && base32.charAt(len - 1) == '=') {
            len--;
        }
        int outLen = (len * 5) / 8;
        byte[] bytes = new byte[outLen];
        int buffer = 0;
        int next = 0;
        int bitsLeft = 0;
        for (int i = 0; i < len; i++) {
            int val = CHAR_MAP[base32.charAt(i)];
            if (val < 0) {
                throw new IllegalArgumentException("Illegal character: " + base32.charAt(i));
            }
            buffer = (buffer << 5) | val;
            bitsLeft += 5;
            if (bitsLeft >= 8) {
                bytes[next++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }
        return bytes;
    }
}
