package com.utilitywatch.login;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserUpdateTest {

    private User user;

    @BeforeEach
    void setUp() {
        User user = new User("test@gmail.com", "password123");
        user.setPropertyAddress("123 Sesame Street");
    }

    @Test
    void ShouldThrowExceptionAtDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("test@gmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtDuplicatePassword() {
        assertThrows(IllegalArgumentException.class, () -> user.setPassword("password123"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutAtSign() {
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("testgmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutUserName() {
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("@gmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutDomain() {
        assertThrows(IllegalArgumentException.class, () -> user.setEmail("test@gmail"));
    }

    @Test
    void ShouldCreateUserWithCorrectCredentialsAndHashedPassword() {
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("password123".hashCode(), user.getPassword());
    }

    @Test
    void ShouldUpdateUser() {
        user.setEmail("Updated@gmail.com");
        user.setPassword("2ndPassword");
        user.setPropertyAddress("101 Real Street");
        assertEquals("Updated@gmail.com", user.getEmail());
        assertEquals("2ndPassword".hashCode(), user.getPassword());
        assertEquals("101 Real Street", user.getPropertyAddress());
    }

    @Test
    void ShouldReusePreviousValueIfEmpty() {
        user.setEmail("");
        user.setPassword("");
        user.setPropertyAddress("");
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("password123".hashCode(), user.getPassword());
        assertEquals("123 Sesame Street", user.getPropertyAddress());
    }
}