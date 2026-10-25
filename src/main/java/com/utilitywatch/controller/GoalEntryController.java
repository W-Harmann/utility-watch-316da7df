package com.utilitywatch.controller;

import com.utilitywatch.goals.Goal;
import com.utilitywatch.goals.GoalProgressCalculator;
import javafx.fxml.FXML;
import javafx.scene.control.*;

/**
 * Controller for the Goal entries ({@code goal-entry.fxml}). Shows the user their goals and progress.
 */
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
    private ProgressBar goalProgressBar;
    @FXML
    private Label goalContainerStatus;
    @FXML
    private Button editGoalDetails;
    @FXML
    private Button deleteGoal;

    private final GoalProgressCalculator progressCalculator = new GoalProgressCalculator();

    // displays goal details and connects the edit/delete buttons to their actions (us8.2, us8.4, us8.5)
    // hasComparisonData is false when the previous period has no readings (us8.3)
    public void setData (Goal goal, boolean hasComparisonData, Runnable onEdit, Runnable onDelete) {
        boolean isEnergy = "energy".equals(goal.getResourceType());
        goalContainerResource.setText(isEnergy ? "Energy" : "Water");
        goalContainerTarget.setText("Target: " + (int) goal.getTargetReduction() + "% reduction");
        goalContainerPeriod.setText("Period: " + goal.getTargetPeriod());

        // colours the progress bar to match the resource (us8.3)
        goalProgressBar.getStyleClass().add(isEnergy ? "goal-progress-energy" : "goal-progress-water");

        // us8.3 no previous readings to compare, so show a message instead of a misleading 0%
        if (!hasComparisonData && !goal.isCompleted()) {
            goalContainerProgress.setText("Progress: no comparison data yet");
            goalProgressBar.setProgress(0.0);
            goalContainerStatus.setText("Record readings for this period and the one before it to track this goal.");
        } else {
            // us8.4 shows the real reduction achieved and marks completed goals
            String progress = String.format("Progress: %.0f%% reduction achieved", goal.getCurrentProgress());
            goalContainerProgress.setText(goal.isCompleted() ? progress + " (Completed)" : progress);

            // us8.3 fills the bar based on how close the goal is to its target
            goalProgressBar.setProgress(progressCalculator.progressFraction(goal.getCurrentProgress(), goal.getTargetReduction()));

            // us8.3 shows whether the goal is achieved, on track, or behind
            GoalProgressCalculator.Status status = goal.isCompleted() ? GoalProgressCalculator.Status.COMPLETED
                    : progressCalculator.status(goal.getCurrentProgress(), goal.getTargetReduction());

            switch (status) {
                case COMPLETED:
                    goalContainerStatus.setText("Goal achieved!");
                    break;
                case ON_TRACK:
                    goalContainerStatus.setText("On track - keep it up!");
                    break;
                case BEHIND:
                    goalContainerStatus.setText("Behind target - try reducing usage further.");
                    break;
            }
        }

        // runs the edit and delete actions when their buttons are clicked
        editGoalDetails.setOnAction(event -> onEdit.run());
        deleteGoal.setOnAction(event -> onDelete.run());
    }
}
