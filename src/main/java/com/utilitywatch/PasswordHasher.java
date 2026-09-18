package com.utilitywatch;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class PasswordHasher {

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