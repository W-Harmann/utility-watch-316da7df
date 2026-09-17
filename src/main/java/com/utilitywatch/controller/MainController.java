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
        showReportsOverview();
    }

    // loads the reports screen into the main content area
    // public so other screens can call this to navigate back here
    public void showReportsOverview() {
        loadScreen("/com/utilitywatch/reports/reports-view.fxml");
    }

    // public so other controllers can use this method to navigate to the customise report screen
    public void showCustomiseReport() {
        loadScreen("/com/utilitywatch/reports/customise-report-view.fxml");
    }

    // shared method for loading screens into the main content area
    private void loadScreen(String fxmlPath) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent view = fxmlLoader.load();

            // replace whatever is currently shown with the newly loaded screen
            contentArea.getChildren().setAll(view);

            // get the controller for the loaded fxml screen
            Object controller = fxmlLoader.getController();

            // check the controller type and give it access to MainController for navigation
            if (controller instanceof ReportsController reportsController) {
                reportsController.setMainController(this);
            } else if (controller instanceof CustomiseReportController customiseController) {
                customiseController.setMainController(this);
            }
        } catch (IOException e) {
            contentArea.getChildren().setAll(new Label("Failed to load screen"));
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