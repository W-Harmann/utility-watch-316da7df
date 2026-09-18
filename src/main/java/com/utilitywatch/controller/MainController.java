package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;
import com.utilitywatch.login.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private Label contentLabel;

    @FXML
    private VBox contentArea;

    private final AuthenticationService authService = SharedAuth.getService();

    @FXML
    protected void onHomeClick() {
        showLabel("Home");
    }

    @FXML
    protected void onReadingsClick() {
        try {
            Parent readingsView = FXMLLoader.load(getClass().getResource("/com/utilitywatch/readings-view.fxml"));
            contentArea.getChildren().setAll(readingsView);
        } catch (IOException e) {
            showLabel("Couldn't load Readings");
        }
    }

    @FXML
    protected void onReportsClick() {
        showLabel("Reports");
    }

    @FXML
    protected void onGoalsClick() {
        showLabel("Goals");
    }

    @FXML
    protected void onCostsClick() {
        showLabel("Costs");
    }

    @FXML
    protected void onImpactClick() {
        showLabel("Impact");
    }

    @FXML
    protected void onAlertsClick() {
        showLabel("Alerts");
    }

    @FXML
    protected void onSettingsClick() {
        showLabel("Settings");
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

    // restores the plain placeholder label as the content area's only child -
    // needed since Readings replaces it with a real sub-view
    private void showLabel(String text) {
        contentLabel.setText(text);
        contentArea.getChildren().setAll(contentLabel);
    }
}