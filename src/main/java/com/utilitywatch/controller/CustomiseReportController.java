package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.event.ActionEvent;

public class CustomiseReportController {

    @FXML
    private VBox previewContainer;
    @FXML
    private CheckBox selectAllCheckBox;
    @FXML
    private CheckBox consumptionStatisticsCheckBox;
    @FXML
    private CheckBox trendsAnalysisCheckBox;
    @FXML
    private CheckBox comparisonsCheckBox;
    @FXML
    private CheckBox unusualPatternsCheckBox;
    @FXML
    private CheckBox conservationOpportunitiesCheckBox;
    @FXML
    private CheckBox costSummaryCheckBox;
    @FXML
    private CheckBox environmentalImpactCheckBox;

    // reference back to MainController to navigate to other screens
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // all the customise report option checkboxes excluding select all
    private CheckBox[] getReportOptionCheckboxes() {
        return new CheckBox[]{
            consumptionStatisticsCheckBox,
            trendsAnalysisCheckBox,
            comparisonsCheckBox,
            unusualPatternsCheckBox,
            conservationOpportunitiesCheckBox,
            costSummaryCheckBox,
            environmentalImpactCheckBox
        };
    }

    // updates the preview label to match the checkbox state
    private void updatePreviewForCheckbox(CheckBox optionCheckbox) {
        String optionName = optionCheckbox.getText();

        // removes existing label to prevent duplicate preview labels
        previewContainer.getChildren().removeIf(existingPreviewLabel -> optionName.equals(existingPreviewLabel.getUserData()));

        // adds the preview label when the option is selected
        if (optionCheckbox.isSelected()) {
            Label previewLabel = new Label(optionName);
            previewLabel.setUserData(optionName);
            previewContainer.getChildren().add(previewLabel);
        }
    }

    // called by any checkbox when its clicked and updates its matching preview label
    @FXML
    private void handleCheckboxAction(ActionEvent checkboxClickEvent) {
        updatePreviewForCheckbox((CheckBox) checkboxClickEvent.getSource());

        // checks the select all checkbox only when every option is selected
        boolean allSelected = true;
        for (CheckBox optionCheckbox : getReportOptionCheckboxes()) {
            if (!optionCheckbox.isSelected()) {
                allSelected = false;
            }
        }
        selectAllCheckBox.setSelected(allSelected);
    }

    // checks or unchecks all customise report options together
    @FXML
    private void onSelectAllClick() {
        boolean shouldSelectAll = selectAllCheckBox.isSelected();

        for (CheckBox optionCheckbox : getReportOptionCheckboxes()) {
            optionCheckbox.setSelected(shouldSelectAll);
            updatePreviewForCheckbox(optionCheckbox);
        }
    }

    // sends user back to the reports overview screen
    @FXML
    protected void onCancelClick() {
        if (mainController != null) {
            mainController.showReportsOverview();
        }
    }

    // TODO: add functionality to validate that at least one checkbox is selected, then generate / export the report
    // temporary placeholder for report validation and generation
    @FXML
    protected void onGenerateReportClick() {
        System.out.println("Generate report button clicked from customise report screen");
    }
}
