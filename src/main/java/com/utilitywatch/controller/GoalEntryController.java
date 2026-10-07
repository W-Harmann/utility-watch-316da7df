package com.utilitywatch.controller;

import com.utilitywatch.goals.Goal;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GoalEntryController {
    @FXML
    private Label goalContainerResource;
    @FXML
    private Label goalContainerTarget;
    @FXML
    private Label goalContainerPeriod;
    @FXML
    private Label goalContainerProgress;

    public void setData (Goal goal) {
        String resourceLabel = "energy".equals(goal.getResourceType()) ? "Energy" : "Water";
        goalContainerResource.setText(resourceLabel);
        goalContainerTarget.setText("Target: " + (int) goal.getTargetReduction() + "% reduction");
        goalContainerPeriod.setText("Period: " + goal.getTargetPeriod());

        // TODO: connect real progress tracking once consumption data is linked to gaols
        goalContainerProgress.setText("Progress: 0%");
    }
}
