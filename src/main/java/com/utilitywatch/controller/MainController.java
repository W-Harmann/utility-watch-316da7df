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
import java.util.Objects;

/**
 * The main controller shared on every page for navigation ({@code main-view.fxml}). Shows the navbar on every page. The
 * home page can directly open home, readings, reports, goals, costs, impacts, notifications, settings, and settings
 * pages. Can also log out the user.
 */
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
    private ToggleButton notificationsNavButton;

    private final AuthenticationService authService = SharedAuth.getService();

    /** Loads the home dashboard screen straight after login. Called by the FXML loader. */
    @FXML
    public void initialize() {
        onHomeClick();
    }

    @FXML
    protected void onHomeClick() {
        homeNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/home-view.fxml");
    }

    /** Shows the home dashboard. Public so other screens, like Customise Report, can return to it. */
    public void showHome() {
        onHomeClick();
    }

    @FXML
    protected void onReadingsClick() {
        loadScreen("/com/utilitywatch/readings-view.fxml");
    }

    @FXML
    protected void onReportsClick() {
        showReportsOverview();
    }

    /**
     * Loads the Reports screen into the main content area and highlights its navbar button. Public so
     * other screens can navigate back here.
     */
    public void showReportsOverview() {
        reportsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/reports/reports-view.fxml");
    }

    /** Loads the Customise Report (export) screen into the main content area. */
    public void showCustomiseReport() {
        loadScreen("/com/utilitywatch/reports/customise-report-view.fxml");
    }

    @FXML
    protected void onGoalsClick() {
        showGoals();
    }

    /** Loads the Conservation screen into the main content area. */
    public void showConservation() {
        loadScreen("/com/utilitywatch/conservation-view.fxml");
    }

    /**
     * Loads the Goals screen into the main content area and highlights its navbar button. Public so the
     * home screen's goals tile can navigate here.
     */
    public void showGoals() {
        goalsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/goals-view.fxml");
    }

    @FXML
    protected void onCostsClick() {
        showCosts();
    }

    /**
     * Loads the Costs screen into the main content area and highlights its navbar button, including when
     * navigation comes from the home screen's costs tile.
     */
    public void showCosts() {
        costsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/costs-view.fxml");
    }

    @FXML
    protected void onImpactClick() {
        loadScreen("/com/utilitywatch/impact-view.fxml");
    }

    @FXML
    protected void onNotificationsClick() {
        showNotifications();
    }

    /** Loads the Notifications screen into the main content area and highlights its navbar button. */
    public void showNotifications() {
        notificationsNavButton.setSelected(true);
        loadScreen("/com/utilitywatch/notifications-view.fxml");
    }

    @FXML
    protected void onSettingsClick() {
        loadScreen("/com/utilitywatch/settings-view.fxml");
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
            Parent root = FXMLLoader.load(Objects.requireNonNull(getClass().getResource("/com/utilitywatch/login-view.fxml")));
            stage.setScene(new Scene(root, 400, 400));
        } catch (IOException e) {
            contentLabel.setText("Couldn't load the login screen");
        }
    }
}
