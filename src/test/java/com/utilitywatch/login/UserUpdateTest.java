package com.utilitywatch.login;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserUpdateTest {

    private User user;

    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        User user = new User("test@gmail.com", "password123");
        user.setPropertyAddress("123 Sesame Street");
    }

    @Test
    void ShouldThrowExceptionAtDuplicateEmail() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateEmail(user, "test@gmail.com", "test@gmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtDuplicatePassword() {
        assertThrows(IllegalArgumentException.class, () -> authService.updatePassword(user, "password123", "password123"));
    }

    @Test
    void ShouldThrowExceptionAtDuplicatePropertyAddress() {
        assertThrows(IllegalArgumentException.class, () -> authService.updatePropertyAddress(user, "123 Sesame Street", "123 Sesame Street"));
    }

    @Test
    void ShouldThrowExceptionAtIncorrectOldEmail() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateEmail(user, "Incorrect@gmail.com", "NewEmail@gmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtIncorrectOldPassword() {
        assertThrows(IllegalArgumentException.class, () -> authService.updatePassword(user, "Incorrect", "newPassword"));
    }

    @Test
    void ShouldThrowExceptionAtIncorrectOldPropertyAddress() {
        assertThrows(IllegalArgumentException.class, () -> authService.updatePropertyAddress(user, "123 Incorrect Street", "101 Fools Road"));
    }

    @Test
    void ShouldThrowExceptionAtExistingEmail() {
        User newUser = authService.signUp("Person@gmail.com", "PaSsWorDExaMple");
        assertThrows(IllegalArgumentException.class, () -> authService.updateEmail(newUser, "Person@gmail.com", "test@gmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutAtSign() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateEmail(user, "test@gmail.com", "NewEmailgmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutUserName() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateEmail(user, "test@gmail.com", "@gmail.com"));
    }

    @Test
    void ShouldThrowExceptionAtEmailWithoutDomain() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateEmail(user, "test@gmail.com", "NewEmail@gmail"));
    }

    @Test
    void ShouldUpdateUser() {
        authService.updateEmail(user, "test@gmail.com", "Updated@gmail.com");
        authService.updatePassword(user, "password123", "2ndPassword");
        authService.updatePropertyAddress(user, "123 Sesame Street", "101 Real Street");
        assertEquals("Updated@gmail.com", user.getEmail());
        assertEquals("2ndPassword".hashCode(), user.getPassword());
        assertEquals("101 Real Street", user.getPropertyAddress());
    }

    @Test
    void ShouldReusePreviousValueIfEmpty() {
        authService.updateEmail(user, "test@gmail.com", "");
        authService.updatePassword(user, "password123", "");
        authService.updatePropertyAddress(user, "123 Sesame Street", "");
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("password123".hashCode(), user.getPassword());
        assertEquals("123 Sesame Street", user.getPropertyAddress());
    }
}