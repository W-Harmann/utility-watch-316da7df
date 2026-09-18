package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;

// one shared AuthenticationService so Login/Register/Main controllers (each
// created fresh by the FXML loader) all see the same registered users.

public class SharedAuth {

    private static final AuthenticationService instance = new AuthenticationService();

    static {
        // dev convenience only - since storage is in-memory, every restart
        // wipes accounts, forcing a fresh register each time.
        instance.signUp("test@test.com", "password123");
    }

    private SharedAuth() {
    }

    public static AuthenticationService getService() {
        return instance;
    }
}