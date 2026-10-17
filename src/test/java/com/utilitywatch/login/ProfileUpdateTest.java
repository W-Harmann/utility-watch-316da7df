package com.utilitywatch.login;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// US13.4 (update profile) and the account-removal part of US12.3, using the in-memory DAO
public class ProfileUpdateTest {

    private InMemoryUserDAO userDAO;
    private AuthenticationService authService;
    private User user;

    @BeforeEach
    void setUp() {
        userDAO = new InMemoryUserDAO();
        authService = new AuthenticationService(userDAO);
        user = authService.signUp("test@gmail.com", "password123");
        user.setName("Sam");
        user.setPropertyAddress("123 Sesame Street");
    }

    @Test
    void ShouldUpdateProfile() {
        authService.updateProfile(user, "Alex", "new@gmail.com", "1 New Road");
        assertEquals("Alex", user.getName());
        assertEquals("new@gmail.com", user.getEmail());
        assertEquals("1 New Road", user.getPropertyAddress());
        assertEquals("Alex", userDAO.findByEmail("new@gmail.com").getName());
    }

    @Test
    void ShouldRefuseBlankNameAndKeepPreviousValue() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(user, "  ", "test@gmail.com", "123 Sesame Street"));
        assertEquals("Sam", user.getName());
    }

    @Test
    void ShouldRefuseBlankEmailAndKeepPreviousValue() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(user, "Sam", "", "123 Sesame Street"));
        assertEquals("test@gmail.com", user.getEmail());
    }

    @Test
    void ShouldRefuseInvalidEmail() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(user, "Sam", "NewEmailgmail.com", "123 Sesame Street"));
        assertEquals("test@gmail.com", user.getEmail());
    }

    @Test
    void ShouldRefuseEmailUsedByAnotherAccount() {
        authService.signUp("other@gmail.com", "password123");
        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(user, "Sam", "other@gmail.com", "123 Sesame Street"));
        assertEquals("test@gmail.com", user.getEmail());
    }

    @Test
    void ShouldAllowKeepingOwnEmail() {
        assertDoesNotThrow(() -> authService.updateProfile(user, "Alex", "test@gmail.com", "123 Sesame Street"));
    }

    @Test
    void ShouldAllowBlankPropertyAddress() {
        authService.updateProfile(user, "Sam", "test@gmail.com", "");
        assertNull(user.getPropertyAddress());
    }

    @Test
    void ShouldChangeNothingWhenAnyFieldIsRefused() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(user, "", "changed@gmail.com", "9 Other Road"));
        assertEquals("Sam", user.getName());
        assertEquals("test@gmail.com", user.getEmail());
        assertEquals("123 Sesame Street", user.getPropertyAddress());
    }

    @Test
    void ShouldDeleteAccountAndEndSession() {
        Session.getInstance().login(user);
        authService.deleteAccount(user);
        assertNull(userDAO.findByEmail("test@gmail.com"));
        assertFalse(Session.getInstance().isLoggedIn());
    }
}