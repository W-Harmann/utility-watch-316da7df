package com.utilitywatch.controller;

import com.utilitywatch.goals.Goal;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;

/** fills in one goal's progress on the home dashboard */
public class DashboardGoalRowController {
    @FXML
    private Label goalNameLabel;
    @FXML
    private Label progressPercentLabel;
    @FXML
    private ProgressBar goalProgressBar;
    @FXML
    private Label goalTargetLabel;

    /**
     * @param goal the goal to show
     * @param progress progress towards the target from 0 to 1, or null if there is no data yet
     */
    public void setData(Goal goal, Double progress) {
        boolean isEnergy = "energy".equals(goal.getResourceType());

        goalNameLabel.setText("Reduce " + (isEnergy ? "Energy" : "Water") + " Usage");
        goalTargetLabel.setText("Target: " + (int) goal.getTargetReduction() + "% reduction (" + goal.getTargetPeriod() + ")");

        // colours the progress bar according to its utility
        progressPercentLabel.getStyleClass().add(isEnergy ? "energy-column-header" : "water-column-header");
        goalProgressBar.getStyleClass().add(isEnergy ? "goal-progress-energy" : "goal-progress-water");

        if (progress == null) {
            progressPercentLabel.setText("No data");
        } else {
            goalProgressBar.setProgress(progress);
            progressPercentLabel.setText(String.format("%.0f%%", progress * 100));
        }
    }
}