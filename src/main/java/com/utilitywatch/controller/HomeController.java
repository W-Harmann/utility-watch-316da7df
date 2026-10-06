package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class HomeController {

    @FXML
    private Label welcomeLabel;

    // references MainController so buttons like the cost tile can navigate to other screens
    private MainController mainController;

    // called when home loads so it can use MainController to navigate to other screens such as costs
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // TODO: add username to register/login for a personalised greeting (if user entered one)
        welcomeLabel.setText("Welcome back!");
    }

    @FXML
    protected void onCostTileClick() {
        // opens the costs screen when the cost tile is clicked
        if (mainController != null) {
            mainController.showCosts();
        }
    }
}