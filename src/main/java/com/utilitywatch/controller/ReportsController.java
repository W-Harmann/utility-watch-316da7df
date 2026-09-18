package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.chart.BarChart;
import javafx.application.Platform;

public class ReportsController {
    // display content
    @FXML
    private VBox overviewContentBox;
    @FXML
    private Label comingSoonLabel;
    @FXML
    private VBox unusualPatternsContentBox;
    @FXML
    private VBox statisticsContentBox;
    // utility toggle buttons
    @FXML
    private ToggleButton energyToggle;
    @FXML
    private ToggleButton waterToggle;
    @FXML
    private ToggleButton bothToggle;
    @FXML
    private ToggleGroup utilityToggleGroup;
    // utility consumption charts
    @FXML
    private LineChart<String, Number> consumptionChart;
    @FXML
    private LineChart<String, Number> unusualConsumptionChart;
    @FXML
    private BarChart<String, Number> distributionChart;

    // reference to MainController for screen navigation
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadMockChartData();
        loadMockUnusualConsumptionData();
        loadMockDistributionData();

        // updates the chart style whenever the utility toggle changes
        utilityToggleGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> updateChartColour());
        // applies the correct chart style when the screen first loads
        updateChartColour();
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

        // creates the water consumption data series and adds each water value to its date
        XYChart.Series<String, Number> waterSeries = new XYChart.Series<>();
        waterSeries.setName("Water (L)");
        for (int day = 0; day < days.size(); day++) {
            waterSeries.getData().add(new XYChart.Data<>(days.get(day), waterValues.get(day)));
        }

        // adds both data series to the consumption chart
        consumptionChart.getData().addAll(energySeries, waterSeries);
    }

    // loads sample unusual consumption data
    private void loadMockUnusualConsumptionData() {
        // prevents duplicate data from being added to the chart
        if (!unusualConsumptionChart.getData().isEmpty()) {
            return;
        }

        List<String> days = new ArrayList<>();
        for(int count = 1; count <= 31; count++){
            days.add("Aug " + count);
        }

        days.add("Sep 1");

        List<Double> values = List.of(15.0, 16.0, 18.0, 15.3, 16.2, 19.4, 15.6, 20.0, 19.5, 18.6, 18.3, 16.0, 17.4, 16.7, 18.2, 18.4, 18.4, 17.0, 19.2, 17.4, 13.5, 11.2, 12.3, 16.3, 17.4, 15.7, 16.6, 18.0, 18.6, 16.0, 17.4, 17.0);

        List<String> flaggedDays = List.of("Aug 21", "Aug 22", "Aug 23");
        List<Double> flaggedValues = List.of(13.5, 11.2, 12.3);

        // creates the energy consumption data series and adds each energy value to its date
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int day = 0; day < days.size(); day++) {
            series.getData().add(new XYChart.Data<>(days.get(day), values.get(day)));
        }

        // creates the water consumption data series and adds each water value to its date
        XYChart.Series<String, Number> flaggedSeries = new XYChart.Series<>();
        for (int day = 0; day < flaggedValues.size(); day++) {
            flaggedSeries.getData().add(new XYChart.Data<>(flaggedDays.get(day), flaggedValues.get(day)));
        }

        // adds both data series to the consumption chart
        unusualConsumptionChart.getData().addAll(series, flaggedSeries);
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
        showUnusualPatterns();
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

        // refreshes the consumption chart layout after the content becomes visible
        Platform.runLater(consumptionChart::requestLayout);
    }

    // shows the unusual patterns tab content and hides everything else
    // currently defaults the utility toggle to 'energy'
    private void showUnusualPatterns() {
        hideAllTabContent();
        unusualPatternsContentBox.setVisible(true);
        unusualPatternsContentBox.setManaged(true);
        loadMockUnusualConsumptionData();

        // refreshes the unusual consumption chart layout after the content becomes visible
        Platform.runLater(unusualConsumptionChart::requestLayout);
    }

    // shows the statistics tab content and hides everything else
    // currently defaults the utility toggle to 'energy'
    private void showStatistics() {
        hideAllTabContent();
        statisticsContentBox.setVisible(true);
        statisticsContentBox.setManaged(true);
        energyToggle.setSelected(true);
        loadMockDistributionData();

        // refreshes the distribution chart layout after the content becomes visible
        Platform.runLater(distributionChart::requestLayout);
    }

    // loads sample consumption distribution data into the chart
    private void loadMockDistributionData() {
        // prevents duplicate data from being added to the chart
        if (!distributionChart.getData().isEmpty()) {
            return;
        }

        // creates a series containing sample utility distribution values
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // adds sample consumption values for each time period
        series.getData().add(new XYChart.Data<>("5-7", 2));
        series.getData().add(new XYChart.Data<>("7-9", 2));
        series.getData().add(new XYChart.Data<>("9-11", 4));
        series.getData().add(new XYChart.Data<>("11-13", 6));
        series.getData().add(new XYChart.Data<>("13-15", 3));
        series.getData().add(new XYChart.Data<>("15-17", 2));
        series.getData().add(new XYChart.Data<>("17-19", 1));

        // adds the sample series to the distribution chart
        distributionChart.getData().add(series);
    }

    // updates the chart colour based on the selected utility toggle
    private void updateChartColour() {

        List<XYChart<String, Number>> allCharts = List.of(distributionChart, unusualConsumptionChart);

        for(XYChart chart: allCharts) {

            // removes any previously applied utility toggle
                chart.getStyleClass().removeAll("energy-selected", "water-selected", "both-selected");

            // applies the specific chart style related to the selected utility type
            if (energyToggle.isSelected()) {
                chart.getStyleClass().add("energy-selected");
            } else if (waterToggle.isSelected()) {
                chart.getStyleClass().add("water-selected");
            } else if (bothToggle.isSelected()) {
                chart.getStyleClass().add("both-selected");
            }
        }
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

        unusualPatternsContentBox.setVisible(false);
        unusualPatternsContentBox.setManaged(false);

        statisticsContentBox.setVisible(false);
        statisticsContentBox.setManaged(false);

        comingSoonLabel.setVisible(false);
        comingSoonLabel.setManaged(false);
    }
}

