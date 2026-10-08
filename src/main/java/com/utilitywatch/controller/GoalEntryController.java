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

        // us8.4 shows the real reduction achieved and marks completed goals
        String progress = String.format("Progress: %.0f%% reduction achieved", goal.getCurrentProgress());
        goalContainerProgress.setText(goal.isCompleted() ? progress + " (Completed)" : progress);
    }
}
