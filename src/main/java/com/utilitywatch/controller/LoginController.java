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

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private final AuthenticationService authService = SharedAuth.getService();

    @FXML
    private void onLoginClick() {
        try {
            authService.logIn(emailField.getText(), passwordField.getText());
            switchToMainView();
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        } catch (IOException e) {
            errorLabel.setText("Couldn't load the main screen");
        }
    }

    @FXML
    private void onRegisterClick() {
        try {
            Stage stage = (Stage) emailField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/utilitywatch/register-view.fxml"));
            stage.setScene(new Scene(root, 400, 400));
        } catch (IOException e) {
            errorLabel.setText("Couldn't load the register screen");
        }
    }

    private void switchToMainView() throws IOException {
        Stage stage = (Stage) emailField.getScene().getWindow();
        Parent root = FXMLLoader.load(getClass().getResource("/com/utilitywatch/main-view.fxml"));
        Scene scene = new Scene(root, 900, 600);
        scene.getStylesheets().add(getClass().getResource("/com/utilitywatch/stylesheet.css").toExternalForm());
        stage.setScene(scene);
    }
}