package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;

// one shared AuthenticationService so Login/Register/Main controllers (each
// created fresh by the FXML loader) all see the same registered users.
// Currently in-memory by default
public class SharedAuth {

    private static final AuthenticationService instance = new AuthenticationService();

    private SharedAuth() {
    }

    public static AuthenticationService getService() {
        return instance;
    }
}