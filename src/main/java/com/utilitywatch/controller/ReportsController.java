package com.utilitywatch.controller;

import com.utilitywatch.Reading;
import com.utilitywatch.consumptionDetector.ActiveHoursDetector;
import com.utilitywatch.consumptionDetector.LongDurationDetector;
import com.utilitywatch.consumptionDetector.SpikeDetector;
import com.utilitywatch.consumptionDetector.UnusualConsumptionDetector;
import com.utilitywatch.database.ReadingDAO;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

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

    private final ReadingDAO readingDAO = SharedReadingDAO.getDAO();

    @FXML
    public void initialize() {
        loadMockChartData();
        loadUnusualConsumptionData();
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
    private void loadUnusualConsumptionData() {
        // prevents duplicate data from being added to the chart
        if (!unusualConsumptionChart.getData().isEmpty()) {
            return;
        }
        User currentUser = Session.getInstance().getCurrentUser();
        int currentUserId = currentUser.getId();

        List<Reading> readings = readingDAO.getReadingsForUser(currentUserId);

        // For the dates to be in ascending order
        Collections.reverse(readings);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        // creates the energy consumption data series and adds each energy value to its date
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        XYChart.Series<String, Number> flaggedSeries = new XYChart.Series<>();

        //TODO remove magic numbers

        ///ActiveHoursDetector activeHoursDetector = new ActiveHoursDetector(LocalTime.of(6,0), LocalTime.of(23, 0));
        //LongDurationDetector longDurationDetector = new LongDurationDetector(18.0, 72);
        ///SpikeDetector spikeDetector = new SpikeDetector(3.0, 3);
        UnusualConsumptionDetector unusualConsumptionDetector = new UnusualConsumptionDetector();

        //A week of data assuming each entry is seperated by a day
        int limit = 7;
        int count = 0;
        List<Double> weeklyReadings;

        for (Reading reading : readings) {
            // TODO make section consider different value based on toggled utility
            if (Objects.equals(reading.getUnit(), "kWh")) {
                weeklyReadings = new ArrayList<>();
                if (count != 0) {
                    for (int i = count - 1; i >= Math.max(0, count - limit); i--) {
                        weeklyReadings.add(readings.get(i).getQuantity());
                    }
                    if (unusualConsumptionDetector.isUnusual(reading.getQuantity(), weeklyReadings)) {
                        flaggedSeries.getData().add(new XYChart.Data<>(reading.getDateTime().format(formatter), reading.getQuantity()));
                    }
                }

                series.getData().add(new XYChart.Data<>(reading.getDateTime().format(formatter), reading.getQuantity()));
                count++;
            }

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
        loadUnusualConsumptionData();

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

