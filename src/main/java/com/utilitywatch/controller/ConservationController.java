package com.utilitywatch.controller;

import javafx.fxml.FXML;

public class ConservationController {

    // reference back to MainController to navigate to other screens
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

    }


    @FXML
    protected void onCancelClick() {
        if (mainController != null) {
            mainController.onHomeClick();
        }
    }


}
