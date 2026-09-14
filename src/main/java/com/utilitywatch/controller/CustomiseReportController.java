package com.utilitywatch.controller;

import javafx.fxml.FXML;

public class CustomiseReportController {

    // reference back to MainController to navigate to other screens
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
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
        System.out.println("Generate report button clicked from customise screen");
    }
}
