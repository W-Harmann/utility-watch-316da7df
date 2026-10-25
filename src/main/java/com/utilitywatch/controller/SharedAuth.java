package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;
import com.utilitywatch.login.SqliteUserDAO;

/**
 * Holds the one shared {@link AuthenticationService}, so the Login, Register, Main and Settings
 * controllers (each created fresh by the FXML loader) all see the same registered users. It is
 * backed by real persistence through {@link SqliteUserDAO}.
 * <p>When this class is first loaded it also registers a known development account
 * ({@code test@test.com}) if one does not exist yet.
 */
public class SharedAuth {

    private static final AuthenticationService instance = new AuthenticationService(new SqliteUserDAO());

    static {
        // dev convenience: a known account that always works for testing.
        // Guarded because storage is now real - without this, the second
        // time ANYONE ever launches the app, this would throw (email
        // already exists) and crash startup entirely.
        try {
            instance.signUp("test@test.com", "password123");
        } catch (IllegalArgumentException e) {
            // already registered from a previous run - fine, ignore
        }
    }

    private SharedAuth() {
    }

    /**
     * Returns the shared authentication service.
     *
     * @return the shared service
     */
    public static AuthenticationService getService() {
        return instance;
    }
}