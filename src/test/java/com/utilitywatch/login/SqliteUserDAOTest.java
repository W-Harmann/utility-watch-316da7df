package com.utilitywatch.login;

import com.utilitywatch.UtilityWatchConnection;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// US13.4 / US12.3 persistence: profile changes survive an app restart, deleted accounts stay deleted.
// Uses a unique email per test and removes it afterwards.
public class SqliteUserDAOTest {

    private SqliteUserDAO userDAO;
    private String email;

    @AfterEach
    void removal() {
        if (userDAO != null && email != null) {
            User leftover = userDAO.findByEmail(email);
            if (leftover != null) {
                userDAO.delete(leftover.getId());
            }
        }
        UtilityWatchConnection.reset();
    }

    @Test
    void ShouldKeepProfileChangesAfterRestart() {
        email = "profile-test-" + System.nanoTime() + "@test.com";
        userDAO = new SqliteUserDAO();
        AuthenticationService authService = new AuthenticationService(userDAO);
        User user = authService.signUp(email, "password123");
        authService.updateProfile(user, "Sam", email, "123 Sesame Street");

        UtilityWatchConnection.reset(); // simulates closing and reopening the app
        userDAO = new SqliteUserDAO();

        User reloaded = userDAO.findByEmail(email);
        assertEquals("Sam", reloaded.getName());
        assertEquals("123 Sesame Street", reloaded.getPropertyAddress());
    }

    @Test
    void ShouldStayDeletedAfterRestart() {
        email = "delete-test-" + System.nanoTime() + "@test.com";
        userDAO = new SqliteUserDAO();
        AuthenticationService authService = new AuthenticationService(userDAO);
        User user = authService.signUp(email, "password123");
        authService.deleteAccount(user);

        UtilityWatchConnection.reset();
        userDAO = new SqliteUserDAO();

        assertNull(userDAO.findByEmail(email));
    }
}