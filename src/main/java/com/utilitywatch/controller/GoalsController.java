package com.utilitywatch.controller;

import com.utilitywatch.goals.*;
import com.utilitywatch.reports.*;
import javafx.application.Platform;
import javafx.fxml.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.*;
import java.util.*;

public class GoalsController {
    @FXML
    public VBox activeGoalContentBox;
    @FXML
    private VBox completedGoalContentBox;
    @FXML
    private VBox goalContainer;
    @FXML
    private VBox completedGoalContainer;
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

    private final GoalProgressCalculator progressCalculator = new GoalProgressCalculator();

    // goals with no previous readings
    // uses object identity because each progress update creates a new goal object
    private final Set<Goal> goalsWithoutData = Collections.newSetFromMap(new IdentityHashMap<>());

    // provides set period options so users select a valid format instead of entering the period as free text
    private static final List<String> PERIODS = List.of("1 Month", "3 Months", "6 Months", "1 Year");

    private final GoalManager goalManager = new GoalManager();

    // shared goal list and progress calculation also used by home dashboard
    private final GoalRepository goalRepository = GoalRepository.getInstance();
    private final GoalProgressService goalProgressService = new GoalProgressService();

    // forget all goals, e.g. when an account is deleted
    public static void clearGoals() {
        goals = new ArrayList<>();
    }

    // reference to MainController for screen navigation
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        periodComboBox.getItems().addAll(PERIODS);
        refreshGoalLists();
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
        try {
            goalRepository.add(goalManager.createGoal(resourceType, targetReduction, targetPeriod));
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
            return;
        }

        // reset the goal entry area for the next entry
        resourceToggleGroup.selectToggle(null);
        targetField.clear();
        periodComboBox.setValue(null);
        // refreshes both lists so the new goal appears instantly
        refreshGoalLists();
    }

    // reads which resource toggle is selected, or null if neither are
    private String getSelectedResourceType() {
        if (energyToggle.isSelected()) return "energy";
        if (waterToggle.isSelected()) return "water";
        return null;
    }

    // us8.4 updates goal progress from readings, sends alerts for newly completed goals, and refreshes both lists
    private void refreshGoalLists() {
        List<Goal> newlyCompletedGoals = new ArrayList<>();
        goalsWithoutData.clear();

        List<Goal> goals = goalRepository.getAll();

        for (int goalIndex = 0; goalIndex < goals.size(); goalIndex++) {
            Goal goal = goals.get(goalIndex);
            // us8.3 compares the latest period with the previous one if available
            OptionalDouble achieved = goalProgressService.achievedReduction(goal);
            double achievedReduction = achieved.orElse(0.0);

            if (goalManager.justCompleted(goal, achievedReduction)) {
                newlyCompletedGoals.add(goal);

                // also saved as an alert so it appears on the alerts screen (us8.4)
                String resource = "energy".equals(goal.getResourceType()) ? "Energy" : "Water";
                GoalAlertStore.add(new GoalAlertStore.GoalAlert(
                        LocalDateTime.now(),
                        "Goal Completed: Reduced " + resource + " Usage",
                        resource + " successfully reduced by " + (int) goal.getTargetReduction()
                                + "% (" + goal.getTargetPeriod() + ")"));
            }

            // update progress and track goals without readings
            Goal updatedGoal = goalManager.updateProgress(goal, achievedReduction);
            goalRepository.set(goalIndex, updatedGoal);
            if (achieved.isEmpty()) {
                goalsWithoutData.add(updatedGoal);
            }
        }

        // reads the list again so the screen shows the updated goals
        List<Goal> updatedGoals = goalRepository.getAll();

        goalContainer.getChildren().clear();
        completedGoalContainer.getChildren().clear();

        for (Goal activeGoal : goalManager.filterActiveGoals(updatedGoals)) {
            addGoalEntry(activeGoal, goalContainer);
        }

        for (Goal completedGoal : goalManager.filterCompletedGoals(updatedGoals)) {
            addGoalEntry(completedGoal, completedGoalContainer);
        }

        // run later so the alert appears after the screen finishes loading
        if (!newlyCompletedGoals.isEmpty()) {
            Platform.runLater(() -> showCompletionAlert(newlyCompletedGoals));
        }
    }

    // displays a message showing the details of each goal that has been completed
    private void showCompletionAlert(List<Goal> completedGoals) {
        StringBuilder message = new StringBuilder();
        for (Goal goal : completedGoals) {
            message.append("You reached your ").append(goal.getResourceType())
                    .append(" reduction goal of ").append((int) goal.getTargetReduction())
                    .append("% (").append(goal.getTargetPeriod()).append(").\n");
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Goal Completed");
        alert.setHeaderText("Congratulations!");
        alert.setContentText(message.toString());
        alert.show();
    }

    // loads a goal entry view for the given goal and adds it to the active goals container
    private void addGoalEntry(Goal goal, VBox container) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/goal-entry.fxml"));
            VBox goalEntry = loader.load();

            GoalEntryController controller = loader.getController();
            controller.setData(goal, !goalsWithoutData.contains(goal), () -> onEditGoal(goal), () -> onDeleteGoal(goal));

            container.getChildren().add(goalEntry);

        } catch (IOException e) {
            container.getChildren().add(new Label("Failed to load goal"));
        }
    }

    // opens an edit window so the user can change the goal target and period
    private void onEditGoal(Goal goal) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Goal");
        dialog.setHeaderText("Edit your " + goal.getResourceType() + " goal");

        TextField targetInput = new TextField(String.valueOf((int) goal.getTargetReduction()));
        ComboBox<String> periodInput = new ComboBox<>();
        periodInput.getItems().addAll(PERIODS);
        periodInput.setValue(goal.getTargetPeriod());

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.addRow(0, new Label("Target reduction (%):"), targetInput);
        grid.addRow(1, new Label("Period:"), periodInput);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) {
            return;
        }

        try {
            double newTarget = Double.parseDouble(targetInput.getText().trim());
            Goal edited = goalManager.editGoal(goal, newTarget, periodInput.getValue());
            goalRepository.replace(goal, edited);
            errorLabel.setText("");
            refreshGoalLists(); // updates progress using the new goal target and period
        } catch (NumberFormatException e) {
            errorLabel.setText("Target reduction must be a number, e.g. 15");
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    // asks for confirmation before removing the goal
    private void onDeleteGoal(Goal goal) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Goal");
        confirm.setHeaderText("Delete this goal?");
        confirm.setContentText("This " + goal.getResourceType() + " goal will be permanently removed.");

        Optional<ButtonType> result = confirm.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            goalRepository.remove(goal);
            refreshGoalLists();
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

    @FXML
    protected void onConservationClick() {
        if (mainController != null) {
            mainController.showConservation();
        }
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
