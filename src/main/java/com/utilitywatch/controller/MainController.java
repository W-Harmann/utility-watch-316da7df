package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class MainController {

    @FXML
    private Label contentLabel;

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
        contentLabel.setText("Log out");
    }
}