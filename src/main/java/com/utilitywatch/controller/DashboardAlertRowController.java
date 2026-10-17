package com.utilitywatch.controller;

import com.utilitywatch.goals.GoalAlertStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.format.DateTimeFormatter;

/** fills in one alert row on the home dashboard using a shortened format */
public class DashboardAlertRowController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d h:mma");

    @FXML
    private Label alertTitleLabel;
    @FXML
    private Label alertMessageLabel;

    /** @param alert the goal alert to show */
    public void setData(GoalAlertStore.GoalAlert alert) {
        // for example: 'Goal Completed: Reduced Water Usage' becomes 'Water goal completed'
        alertTitleLabel.setText(shortTitle(alert.title()));
        alertMessageLabel.setText(alert.time().format(TIME_FORMAT) + " - " + alert.message());
    }

    // picks the resource out of the stored title and builds a short title
    private String shortTitle(String fullTitle) {
        if (fullTitle.contains("Energy")) {
            return "Energy goal completed";
        }
        if (fullTitle.contains("Water")) {
            return "Water goal completed";
        }
        return fullTitle;
    }
}