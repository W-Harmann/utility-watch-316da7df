package com.utilitywatch.controller;

import javafx.fxml.FXML;

public class ReportsController {

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
        System.out.println("Overview tab clicked");
    }

    @FXML
    protected void onTrendsTabClick() {
        System.out.println("Trends tab clicked");
    }

    @FXML
    protected void onComparisonsTabClick() {
        System.out.println("Comparisons tab clicked");
    }

    @FXML
    protected void onUnusualTabClick() {
        System.out.println("Unusual Patterns tab clicked");
    }

    @FXML
    protected void onStatisticsTabClick() {
        System.out.println("Statistics tab clicked");
    }
}

