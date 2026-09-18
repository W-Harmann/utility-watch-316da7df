package com.utilitywatch;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PasswordHasherTest {

    @Test
    void shouldProduceSameHashForSameInput() {
        String hash1 = PasswordHasher.hash("mypassword");
        String hash2 = PasswordHasher.hash("mypassword");
        assertEquals(hash1, hash2);
    }

    @Test
    void shouldProduceDifferentHashesForDifferentInput() {
        String hash1 = PasswordHasher.hash("mypassword");
        String hash2 = PasswordHasher.hash("otherpassword");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void shouldNeverReturnThePlaintextPassword() {
        String hash = PasswordHasher.hash("mypassword");
        assertNotEquals("mypassword", hash);
    }
}