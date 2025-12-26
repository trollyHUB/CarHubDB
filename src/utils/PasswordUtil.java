package utils;

import java.security.MessageDigest;
import java.security.SecureRandom;

public class PasswordUtil {
    private static final SecureRandom RNG = new SecureRandom();

    public static String generateSaltHex(int bytes) {
        byte[] salt = new byte[bytes];
        RNG.nextBytes(salt);
        return toHex(salt);
    }

    public static String sha256Hex(String data) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] out = md.digest(data.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return toHex(out);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String hashPassword(String password, String saltHex) {
        if (password == null) password = "";
        if (saltHex == null) saltHex = "";
        return sha256Hex(password + saltHex);
    }

    public static boolean verifyPassword(String rawPassword, String saltHex, String expectedHashHex) {
        if (expectedHashHex == null || expectedHashHex.isBlank()) return false;
        String hash = hashPassword(rawPassword, saltHex);
        return expectedHashHex.equalsIgnoreCase(hash);
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}

