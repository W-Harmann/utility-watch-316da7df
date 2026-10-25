package com.utilitywatch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Hashes passwords so that the plain text is never stored.
 * <p>Uses a single SHA-256 hash with no salt, so the same password always gives the same hash.
 */
public class PasswordHasher {

    /**
     * Hashes a password with SHA-256.
     *
     * @param plainTextPassword the password to hash
     * @return the hash as 64 lowercase hexadecimal characters
     * @throws RuntimeException if SHA-256 is not available on this Java installation
     */
    public static String hash(String plainTextPassword) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(plainTextPassword.getBytes());

            // convert each byte to its 2-digit hex form so we get a plain string back
            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            // shouldn't happen, every JVM ships with SHA-256
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
}