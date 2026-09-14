package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
public class ReportsController {

    @FXML private VBox overviewContentBox;
    @FXML private Label comingSoonLabel;

    @FXML
    public void initialize() {
        // empty for now
    }

    // temporary tests to make sure the buttons work
    @FXML
    protected void onGenerateReportClick() {
        System.out.println("Generate report button clicked");
    }

    @FXML
    protected void onOverviewTabClick() {
        // show overview content and add it to the layout
        overviewContentBox.setVisible(true);
        overviewContentBox.setManaged(true);
        // hide coming soon content and remove it from the layout
        comingSoonLabel.setVisible(false);
        comingSoonLabel.setManaged(false);
    }

    @FXML
    protected void onTrendsTabClick() {
        showComingSoon();
    }

    @FXML
    protected void onComparisonsTabClick() {
        showComingSoon();
    }

    @FXML
    protected void onUnusualTabClick() {
        showComingSoon();
    }

    @FXML
    protected void onStatisticsTabClick() {
        showComingSoon();
    }

    private void showComingSoon() {
        // hide overview content and remove it from the layout
        overviewContentBox.setVisible(false);
        overviewContentBox.setManaged(false);
        // show coming soon content and add it to the layout
        comingSoonLabel.setVisible(true);
        comingSoonLabel.setManaged(true);
    }
}

