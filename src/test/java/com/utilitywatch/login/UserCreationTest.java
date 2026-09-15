package com.utilitywatch.login;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserCreationTest {

    private User user;

    @BeforeEach
    void setUp() {
        User user = new User("test@gmail.com", "password123");
    }

    @Test
    void ShouldThrowExceptionAtDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> new User("test@gmail.com", "newPassword"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutAtSign() {
        assertThrows(IllegalArgumentException.class, () -> new User("testgmail.com", "password123"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutUserName() {
        assertThrows(IllegalArgumentException.class, () -> new User("@gmail.com", "password123"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutDomain() {
        assertThrows(IllegalArgumentException.class, () -> new User("test@gmail", "password123"));
    }

    @Test
    void ShouldCreateUser() {
        assertNotNull(user);
    }

    @Test
    void ShouldCreateUserWithCorrectCredentialsAndHashedPassword() {
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("password123".hashCode(), user.getPassword());
    }
}
