package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;
import com.utilitywatch.login.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private Label contentLabel;

    private final AuthenticationService authService = SharedAuth.getService();

    @FXML
    protected void onHomeClick() {
        contentLabel.setText("Home");
    }

    @FXML
    protected void onReadingsClick() {
        contentLabel.setText("Readings");
    }

    @FXML
    protected void onReportsClick() {
        contentLabel.setText("Reports");
    }

    @FXML
    protected void onGoalsClick() {
        contentLabel.setText("Goals");
    }

    @FXML
    protected void onCostsClick() {
        contentLabel.setText("Costs");
    }

    @FXML
    protected void onImpactClick() {
        contentLabel.setText("Impact");
    }

    @FXML
    protected void onAlertsClick() {
        contentLabel.setText("Alerts");
    }

    @FXML
    protected void onSettingsClick() {
        contentLabel.setText("Settings");
    }

    @FXML
    protected void onLogOutClick() {
        authService.logOut(Session.getInstance().getCurrentUser());
        try {
            Stage stage = (Stage) contentLabel.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/utilitywatch/login-view.fxml"));
            stage.setScene(new Scene(root, 400, 400));
        } catch (IOException e) {
            contentLabel.setText("Couldn't load the login screen");
        }
    }
}