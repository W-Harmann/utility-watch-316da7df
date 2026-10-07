package com.utilitywatch.controller;

import com.utilitywatch.reports.ConsumptionSummaryCalculator;
import com.utilitywatch.reports.DistributionCalculator;
import com.utilitywatch.reports.ReportsDataService;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
    private TextField titleField;
    @FXML
    private TextField targetField;
    @FXML
    private TextField periodField;

    // reference to MainController for screen navigation
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }


    @FXML
    public void initialize() {

    }


    @FXML
    protected void onCreateNewGoalClick() {

        if (!titleField.getText().trim().isEmpty() && !targetField.getText().trim().isEmpty() && !periodField.getText().trim().isEmpty()) {

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/goal-entry.fxml"));

                VBox goalEntry = loader.load();

                GoalEntryController controller = loader.getController();
                controller.setData(titleField.getText(), Integer.parseInt(targetField.getText()), periodField.getText(), 0);

                goalContainer.getChildren().add(goalEntry);

                titleField.clear();
                targetField.clear();
                periodField.clear();

            } catch (IOException e) {
                activeGoalContentBox.getChildren().setAll(new Label("Failed to goal"));
            }
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
