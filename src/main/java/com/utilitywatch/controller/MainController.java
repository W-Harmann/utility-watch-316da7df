package com.utilitywatch.controller;

import com.utilitywatch.login.AuthenticationService;
import com.utilitywatch.login.Session;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class MainController {

    @FXML
    private Label contentLabel;

    @FXML
    private VBox contentArea;

    // links to the relevant navigation button
    @FXML
    private ToggleButton homeNavButton;
    @FXML
    private ToggleButton reportsNavButton;
    @FXML
    private ToggleButton goalsNavButton;
    @FXML
    private ToggleButton costsNavButton;
    @FXML
    private ToggleButton alertsNavButton;

    private final AuthenticationService authService = SharedAuth.getService();

    // automatically loads the home dashboard screen after login
    @FXML
    public void initialize() {
        onHomeClick();
    }

    @FXML
    protected void onHomeClick() {
        homeNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/home-view.fxml");
    }

    // public so other screens like customise report can return to the dashboard
    public void showHome() {
        onHomeClick();
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
        showReportsOverview();
    }

    // loads the reports screen into the main content area
    // public so other screens can call this to navigate back here
    public void showReportsOverview() {
        reportsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/reports/reports-view.fxml");
    }

    // public so other controllers can use this method to navigate to the customise report screen
    public void showCustomiseReport() {
        loadScreen("/com/utilitywatch/reports/customise-report-view.fxml");
    }

    @FXML
    protected void onGoalsClick() {
        showGoals();
    }

    public void showConservation() {
        loadScreen("/com/utilitywatch/conservation-view.fxml");
    }

    // public so home controller's goals tile can navigate here
    public void showGoals() {
        goalsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/goals-view.fxml");
    }

    @FXML
    protected void onCostsClick() {
        showCosts();
    }

    public void showCosts() {
        // highlights its navbar button when navigation comes from the home screen's costs tile
        costsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/costs-view.fxml");
    }

    @FXML
    protected void onImpactClick() {
        loadScreen("/com/utilitywatch/impact-view.fxml");
    }

    @FXML
    protected void onAlertsClick() {
        showAlerts();
    }

    public void showAlerts() {
        alertsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/alerts-view.fxml");
    }

    @FXML
    protected void onSettingsClick() {
        showLabel("Settings");
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
            } else if (controller instanceof GoalsController goalsController) {
                goalsController.setMainController(this);
            } else if (controller instanceof ConservationController conservationController) {
                conservationController.setMainController(this);
            } else if (controller instanceof HomeController homeController) {
                homeController.setMainController(this);
            }
        } catch (IOException e) {
            contentArea.getChildren().setAll(new Label("Failed to load screen"));
        }
    }

    @FXML
    protected void onLogOutClick() {
        authService.logOut(Session.getInstance().getCurrentUser());
        SharedDateRange.clear();
        try {
            Stage stage = (Stage) contentArea.getScene().getWindow();
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
