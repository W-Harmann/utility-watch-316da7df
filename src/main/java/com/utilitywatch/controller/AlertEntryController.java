package com.utilitywatch.controller;

import com.utilitywatch.goals.GoalAlertStore;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class AlertEntryController {

    @FXML
    private Label alertTitleLabel;
    @FXML
    private Label alertTimeLabel;
    @FXML
    private Label alertMessageLabel;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d yyyy h:mma");

    public void setAlert(GoalAlertStore.GoalAlert alert) {
        alertTitleLabel.setText(alert.title());
        alertTimeLabel.setText(alert.time().format(FORMATTER));
        alertMessageLabel.setText(alert.message());
    }
}

