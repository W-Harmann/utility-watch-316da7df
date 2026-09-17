package com.utilitywatch.login;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class UserLoginTest {

    private User user;
    private AuthenticationService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthenticationService();
        user = authService.signUp("test@gmail.com", "password123");
    }

    @Test
    void ShouldThrowExceptionIncorrectPassword() {
        assertThrows(IllegalArgumentException.class, () -> authService.logIn("test@gmail.com", "differentPassword"));
    }

    @Test
    void ShouldThrowExceptionNonExistentEmail() {
        assertThrows(IllegalArgumentException.class, () -> authService.logIn("NotReal@gmail.com", "password123"));
    }

    @Test
    void ShouldLogInUser() {
        assertEquals(user, authService.logIn("test@gmail.com", "password123"));
    }

    @Test
    void ShouldLogOutUser() {
        User currentUser = authService.logIn("test@gmail.com", "password123");
        assertTrue(authService.logOut(currentUser));
    }
}