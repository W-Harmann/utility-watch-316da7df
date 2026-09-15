package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

public class ReportsController {

    @FXML private VBox overviewContentBox;
    @FXML private Label comingSoonLabel;
    @FXML private VBox statisticsContentBox;

    @FXML private ToggleButton energyToggle;
    @FXML private ToggleButton waterToggle;
    @FXML private ToggleButton bothToggle;

    // reference to MainController for screen navigation
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // empty for now
    }

    // sends user to the customise report screen
    @FXML
    protected void onGenerateReportClick() {
        if (mainController != null) {
            mainController.showCustomiseReport();
        }
    }

    @FXML
    protected void onOverviewTabClick() {
        showOverview();
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
        showStatistics();
    }

    // shows the reports overview tab content and hides everything else
    // defaults the utility toggle to 'both'
    private void showOverview() {
        hideAllTabContent();
        overviewContentBox.setVisible(true);
        overviewContentBox.setManaged(true);
        bothToggle.setSelected(true);
    }

    // shows the statistics tab content and hides everything else
    // currently defaults the utility toggle to 'energy'
    private void showStatistics() {
        hideAllTabContent();
        statisticsContentBox.setVisible(true);
        statisticsContentBox.setManaged(true);
        energyToggle.setSelected(true);
    }

    // shows coming soon placeholder and hides everything else
    private void showComingSoon() {
        hideAllTabContent();
        comingSoonLabel.setVisible(true);
        comingSoonLabel.setManaged(true);
    }

    // hides all tab content and removes it from the layout
    // each show() method can then show only the needed content for that tab
    private void hideAllTabContent() {
        overviewContentBox.setVisible(false);
        overviewContentBox.setManaged(false);

        statisticsContentBox.setVisible(false);
        statisticsContentBox.setManaged(false);

        comingSoonLabel.setVisible(false);
        comingSoonLabel.setManaged(false);
    }
}

