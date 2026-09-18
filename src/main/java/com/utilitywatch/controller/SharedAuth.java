package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;
import com.utilitywatch.login.SqliteUserDAO;

// one shared AuthenticationService so Login/Register/Main controllers (each
// created fresh by the FXML loader) all see the same registered users.
// Backed by real persistence via SqliteUserDAO.
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

    public static AuthenticationService getService() {
        return instance;
    }
}