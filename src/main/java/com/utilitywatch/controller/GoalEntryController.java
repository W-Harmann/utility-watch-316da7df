package com.utilitywatch.controller;

import com.utilitywatch.goals.Goal;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
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
    @FXML
    private Button editGoalDetails;
    @FXML
    private Button deleteGoal;

    // displays goal details and connects the edit/delete buttons to their actions (us8.2, us8.4, us8.5)
    public void setData (Goal goal, Runnable onEdit, Runnable onDelete) {
        String resourceLabel = "energy".equals(goal.getResourceType()) ? "Energy" : "Water";
        goalContainerResource.setText(resourceLabel);
        goalContainerTarget.setText("Target: " + (int) goal.getTargetReduction() + "% reduction");
        goalContainerPeriod.setText("Period: " + goal.getTargetPeriod());

        // us8.4 shows the real reduction achieved and marks completed goals
        String progress = String.format("Progress: %.0f%% reduction achieved", goal.getCurrentProgress());
        goalContainerProgress.setText(goal.isCompleted() ? progress + " (Completed)" : progress);

        // runs the edit and delete actions when their buttons are clicked
        editGoalDetails.setOnAction(event -> onEdit.run());
        deleteGoal.setOnAction(event -> onDelete.run());
    }
}
