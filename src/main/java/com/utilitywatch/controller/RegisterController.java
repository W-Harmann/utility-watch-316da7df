package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the register screen ({@code register-view.fxml}). It creates an account through the
 * shared {@link com.utilitywatch.login.AuthenticationService} and then returns to the login screen.
 */
public class RegisterController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthenticationService authService = SharedAuth.getService();

    @FXML
    private void onRegisterClick() {
        try {
            authService.signUp(emailField.getText(), passwordField.getText());
            goToLogin();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (IOException e) {
            errorLabel.setText("Couldn't load the login screen");
        }
    }

    @FXML
    private void onLoginClick() {
        try {
            goToLogin();
        } catch (IOException e) {
            errorLabel.setText("Couldn't load the login screen");
        }
    }

    private void goToLogin() throws IOException {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/com/utilitywatch/login-view.fxml"));
        stage.setScene(new Scene(root, 400, 400));
    }
}