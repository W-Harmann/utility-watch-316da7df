package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class GoalEntryController {
    @FXML
    private Label goalContainerTitle;
    @FXML
    private Label goalContainerTarget;
    @FXML
    private Label goalContainerPeriod;
    @FXML
    private Label goalContainerProgress;

    public void setData (String title, Integer target, String period, Integer progress) {
        goalContainerTitle.setText(title);
        goalContainerTarget.setText("Target: " + target + "% reduction");
        goalContainerPeriod.setText("Period: " + period);
        goalContainerProgress.setText("Progress: " + progress + "%");
    }
}
