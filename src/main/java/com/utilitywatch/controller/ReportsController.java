package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import java.util.List;

public class ReportsController {

    @FXML private VBox overviewContentBox;
    @FXML private Label comingSoonLabel;
    @FXML private VBox statisticsContentBox;

    @FXML private ToggleButton energyToggle;
    @FXML private ToggleButton waterToggle;
    @FXML private ToggleButton bothToggle;

    @FXML private LineChart<String, Number> consumptionChart;

    // reference to MainController for screen navigation
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadMockChartData();
    }

    // loads sample energy and water consumption data
    private void loadMockChartData() {
        List<String> days = List.of("Aug 1", "Aug 8", "Aug 15", "Aug 22");
        List<Double> energyValues = List.of(300.0, 450.0, 380.0, 500.0);
        List<Double> waterValues = List.of(200.0, 150.0, 300.0, 250.0);

        // creates the energy consumption data series and adds each energy value to its date
        XYChart.Series<String, Number> energySeries = new XYChart.Series<>();
        energySeries.setName("Energy (kWh)");
        for (int day = 0; day < days.size(); day++) {
            energySeries.getData().add(new XYChart.Data<>(days.get(day), energyValues.get(day)));
        }

        // creates the water consumption data series and adds each energy value to its date
        XYChart.Series<String, Number> waterSeries = new XYChart.Series<>();
        waterSeries.setName("Water (L)");
        for (int day = 0; day < days.size(); day++) {
            waterSeries.getData().add(new XYChart.Data<>(days.get(day), waterValues.get(day)));
        }

        // adds both data series to the consumption chart
        consumptionChart.getData().addAll(energySeries, waterSeries);
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

