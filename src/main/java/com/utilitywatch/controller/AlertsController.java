package com.utilitywatch.controller;

import com.utilitywatch.goals.GoalAlertStore;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class AlertsController {

    @FXML
    private VBox recentAlertsContainer;

    // adds completed goal notifications to the top of the recent alerts list (us8.4)
    @FXML
    public void initialize() {
        for (GoalAlertStore.GoalAlert alert : GoalAlertStore.getAll()) {
            // index 0 to put the newest alert at the top
            recentAlertsContainer.getChildren().add(0, createAlertEntryNode(alert));
        }
    }

    // returns each alert as a node so it can be displayed as an individual entry in the alerts list
    private Node createAlertEntryNode(GoalAlertStore.GoalAlert alert) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/alert-entry.fxml"));
            Node alertEntry = loader.load();
            AlertEntryController controller = loader.getController();
            controller.setAlert(alert);
            return alertEntry;
        } catch (IOException e) {
            return new Label("Failed to load alert");
        }
    }

    @FXML
    protected void onViewAllAlerts() {
    }
}
