package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainController {

    @FXML
    private VBox contentArea;

    @FXML
    protected void onHomeClick() {
        contentArea.getChildren().setAll(new Label("Home"));
    }

    @FXML
    protected void onReadingsClick() {
        contentArea.getChildren().setAll(new Label("Readings"));
    }

    @FXML
    protected void onReportsClick() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/com/utilitywatch/reports-overview-view.fxml"));
            Parent reportsOverviewView = fxmlLoader.load();
            contentArea.getChildren().setAll(reportsOverviewView);
        } catch (IOException e) {
            contentArea.getChildren().setAll(new Label("Failed to load reports screen"));
        }
    }

    @FXML
    protected void onGoalsClick() {
        contentArea.getChildren().setAll(new Label("Goals"));
    }

    @FXML
    protected void onCostsClick() {
        contentArea.getChildren().setAll(new Label("Costs"));
    }

    @FXML
    protected void onImpactClick() {
        contentArea.getChildren().setAll(new Label("Impact"));
    }

    @FXML
    protected void onAlertsClick() {
        contentArea.getChildren().setAll(new Label("Alerts"));
    }

    @FXML
    protected void onSettingsClick() {
        contentArea.getChildren().setAll(new Label("Settings"));
    }

    @FXML
    protected void onLogOutClick() {
        contentArea.getChildren().setAll(new Label("Log out"));
    }
}