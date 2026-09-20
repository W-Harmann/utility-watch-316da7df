package com.utilitywatch.login;

import com.utilitywatch.PasswordHasher;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserCreationTest {

    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthenticationService();
        authService.signUp("test@gmail.com", "password123");
    }

    @Test
    void ShouldThrowExceptionAtDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> authService.signUp("test@gmail.com", "newPassword"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutAtSign() {
        assertThrows(IllegalArgumentException.class, () -> authService.signUp("testgmail.com", "password123"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutUserName() {
        assertThrows(IllegalArgumentException.class, () -> authService.signUp("@gmail.com", "password123"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutDomain() {
        assertThrows(IllegalArgumentException.class, () -> authService.signUp("test@gmail", "password123"));
    }

    @Test
    void ShouldCreateUser() {
        User newUser = authService.signUp("Person@gmail.com", "PaSsWorDExaMple");
        assertNotNull(newUser);
    }

    @Test
    void ShouldCreateUserWithCorrectCredentialsAndHashedPassword() {
        User newUser = authService.signUp("Person@gmail.com", "PaSsWorDExaMple");
        assertEquals("Person@gmail.com", newUser.getEmail());
        assertEquals(PasswordHasher.hash("PaSsWorDExaMple"), newUser.getPassword());
    }
}