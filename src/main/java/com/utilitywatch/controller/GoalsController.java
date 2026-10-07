package com.utilitywatch.controller;

import com.utilitywatch.goals.Goal;
import com.utilitywatch.goals.GoalManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class GoalsController {
    @FXML
    public VBox activeGoalContentBox;
    @FXML
    private VBox completedGoalContentBox;
    @FXML
    private VBox goalContainer;
    @FXML
    private ToggleGroup resourceToggleGroup;
    @FXML
    private ToggleButton energyToggle;
    @FXML
    private ToggleButton waterToggle;
    @FXML
    private TextField targetField;
    @FXML
    private ComboBox<String> periodComboBox;
    @FXML
    private Label errorLabel;

    private final GoalManager goalManager = new GoalManager();

    @FXML
    public void initialize() {
        // provides set period options so users select a valid format instead of entering the period as free text
        periodComboBox.getItems().addAll("1 Month", "3 Months", "6 Months", "1 Year");
    }


    @FXML
    protected void onCreateNewGoalClick() {
        errorLabel.setText("");

        // us8.1 ensure the user can select energy or water
        String resourceType = getSelectedResourceType();
        if (resourceType == null) {
            errorLabel.setText("Please select Energy or Water.");
            return;
        }

        // us8.1 validate the entered values are valid (e.g. target is a number)
        double targetReduction;
        try {
            targetReduction = Double.parseDouble(targetField.getText().trim());
        } catch (NumberFormatException e) {
            errorLabel.setText("Target reduction must be a number, e.g. 15");
            return;
        }

        // us8.1 allow the user to select a target period
        String targetPeriod = periodComboBox.getValue();
        if (targetPeriod == null) {
            errorLabel.setText("Please select a target period.");
            return;
        }

        // us8.1 goalManager validates resource type, target, and period and returns a goal only when all checks pass
        Goal newGoal;
        try {
            newGoal = goalManager.createGoal(resourceType, targetReduction, targetPeriod);
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
            return;
        }

        addGoalToContainer(newGoal);

        // reset the goal entry area for the next entry
        resourceToggleGroup.selectToggle(null);
        targetField.clear();
        periodComboBox.setValue(null);
    }

    // reads which resource toggle is selected, or null if neither are
    private String getSelectedResourceType() {
        if (energyToggle.isSelected()) return "energy";
        if (waterToggle.isSelected()) return "water";
        return null;
    }

    // loads a goal entry view for the given goal and adds it to the active goals container
    private void addGoalToContainer(Goal goal) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/goal-entry.fxml"));

            VBox goalEntry = loader.load();

            GoalEntryController controller = loader.getController();
            controller.setData(goal);

            goalContainer.getChildren().add(goalEntry);

        } catch (IOException e) {
            activeGoalContentBox.getChildren().setAll(new Label("Failed to load goal"));
        }
    }

    @FXML
    protected void onActiveGoalsClick() {
        showActiveGoals();
    }

    @FXML
    protected void onCompletedGoalsClick() {
        showCompletedGoals();
    }


    private void showActiveGoals() {
        hideAllTabContent();
        activeGoalContentBox.setVisible(true);
        activeGoalContentBox.setManaged(true);
    }

    private void showCompletedGoals() {
        hideAllTabContent();
        completedGoalContentBox.setVisible(true);
        completedGoalContentBox.setManaged(true);
    }

    // hides all tab content and removes it from the layout
    // each show() method can then show only the needed content for that tab
    private void hideAllTabContent() {
        activeGoalContentBox.setVisible(false);
        activeGoalContentBox.setManaged(false);

        completedGoalContentBox.setVisible(false);
        completedGoalContentBox.setManaged(false);
    }
}
