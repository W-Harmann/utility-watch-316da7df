package com.utilitywatch.controller;

import com.utilitywatch.reports.DistributionCalculator;
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
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import com.utilitywatch.Reading;
import com.utilitywatch.login.Session;
import com.utilitywatch.reports.ConsumptionSummaryCalculator;
import com.utilitywatch.reports.ReadingFilter;

public class ReportsController {
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // display content
    @FXML
    private VBox overviewContentBox;
    @FXML
    private Label comingSoonLabel;
    @FXML
    private VBox statisticsContentBox;
    @FXML
    private PeriodPickerController periodPickerController;
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
    private BarChart<String, Number> distributionChart;

    // consumption stats labels
    @FXML
    private Label totalEnergyLabel;
    @FXML
    private Label totalWaterLabel;
    @FXML
    private Label avgEnergyLabel;
    @FXML
    private Label avgWaterLabel;
    @FXML
    private Label minEnergyLabel;
    @FXML
    private Label minWaterLabel;
    @FXML
    private Label maxEnergyLabel;
    @FXML
    private Label maxWaterLabel;
    @FXML
    private Label medianValueLabel;
    @FXML
    private Label stdDevValueLabel;
    @FXML
    private Label mostCommonRangeValueLabel;
    @FXML
    private Label variabilityValueLabel;
    @FXML
    private Label daysRecordedValueLabel;

    // reference to MainController for screen navigation
    private MainController mainController;

    private final ConsumptionSummaryCalculator calculator = new ConsumptionSummaryCalculator();
    private final DistributionCalculator distributionCalculator = new DistributionCalculator();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // gets the id of the user who is currently logged in and finds only that user's readings
    private int getCurrentUserId() {
        return Session.getInstance().getCurrentUser().getId();
    }

    // gets all readings for the current user then filters them by resource type and the selected date period
    // only the reading quantities are returned as numbers and these values are used by the consumption summary and report generator
    private List<Double> getReadingQuantities(String resourceType) {
        List<Reading> allUserReadings = SharedReadingDAO.getDAO().getReadingsForUser(getCurrentUserId());
        List<Reading> filtered = ReadingFilter.byTypeAndPeriod(allUserReadings, resourceType, periodStart, periodEnd);
        return filtered.stream().map(Reading::getQuantity).toList();
    }

    // gets the user's full readings for a resource type within the selected period
    private List<Reading> getReadings(String resourceType) {
        List<Reading> allUserReadings = SharedReadingDAO.getDAO().getReadingsForUser(getCurrentUserId());
        return ReadingFilter.byTypeAndPeriod(allUserReadings, resourceType, periodStart, periodEnd);
    }

    // rounds a value to one decimal place for display
    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    @FXML
    public void initialize() {
        // updates the chart style whenever the utility toggle changes
        utilityToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateOverviewData();
            if (statisticsContentBox.isVisible()) {
                updateStatisticsData();
            }
        });

        // period start and end for period date picker
        periodStart = periodPickerController.getStart();
        periodEnd = periodPickerController.getEnd();
        periodPickerController.setOnPeriodChanged(this::handlePeriodChanged);

        updateOverviewData();
    }

    // called whenever the user picks a new from/to date in the period picker
    private void handlePeriodChanged() {
        periodStart = periodPickerController.getStart();
        periodEnd = periodPickerController.getEnd();
        updateOverviewData();
        if (statisticsContentBox.isVisible()) {
            updateStatisticsData();
        }
    }

    // loads the user's energy and water readings for the selected period and then updates the chart and summary statistics
    private void updateOverviewData() {
        List<Double> energyReadings = getReadingQuantities("energy");
        List<Double> waterReadings = getReadingQuantities("water");

        updateChart(energyReadings, waterReadings);
        updateStatLabels(energyReadings, waterReadings);
    }

    // clears the chart and adds the selected energy and/or water readings as series
    private void updateChart(List<Double> energyReadings, List<Double> waterReadings) {
        // remove any existing data before displaying the updated readings
        consumptionChart.getData().clear();

        // check which resource toggles are currently selected
        boolean showEnergy = bothToggle.isSelected() || energyToggle.isSelected();
        boolean showWater = bothToggle.isSelected() || waterToggle.isSelected();

        // add the energy readings to the chart if energy is selected and data is available
        if (showEnergy && !energyReadings.isEmpty()) {
            XYChart.Series<String, Number> energySeries = new XYChart.Series<>();
            energySeries.setName("Energy (kWh)");
            // add each energy reading as a separate data point
            for (int energyReadingIndex = 0; energyReadingIndex < energyReadings.size(); energyReadingIndex++) {
                energySeries.getData().add(new XYChart.Data<>("Reading " + (energyReadingIndex + 1), energyReadings.get(energyReadingIndex)));
            }
            consumptionChart.getData().add(energySeries);
        }

        // add the water readings to the chart if water is selected and data is available
        if (showWater && !waterReadings.isEmpty()) {
            XYChart.Series<String, Number> waterSeries = new XYChart.Series<>();
            waterSeries.setName("Water (L)");
            // add each water reading as a separate data point
            for (int waterReadingIndex = 0; waterReadingIndex < waterReadings.size(); waterReadingIndex++) {
                waterSeries.getData().add(new XYChart.Data<>("Reading " + (waterReadingIndex + 1), waterReadings.get(waterReadingIndex)));
            }
            consumptionChart.getData().add(waterSeries);
        }

        Platform.runLater(consumptionChart::requestLayout);
    }

    // calculates the total, average, minimum, and maximum values and updates the lables
    private void updateStatLabels(List<Double> energyReadings, List<Double> waterReadings) {
        if (!energyReadings.isEmpty()) {
            totalEnergyLabel.setText("Total Energy: " + round(calculator.calculateTotal(energyReadings)) + " kWh");
            avgEnergyLabel.setText(String.valueOf(round(calculator.calculateAverage(energyReadings))));
            minEnergyLabel.setText(String.valueOf(round(calculator.calculateMinimum(energyReadings))));
            maxEnergyLabel.setText(String.valueOf(round(calculator.calculateMaximum(energyReadings))));
        } else {
            totalEnergyLabel.setText("Total Energy: no data for this period");
            avgEnergyLabel.setText("-");
            minEnergyLabel.setText("-");
            maxEnergyLabel.setText("-");
        }

        if (!waterReadings.isEmpty()) {
            totalWaterLabel.setText("Total Water: " + round(calculator.calculateTotal(waterReadings)) + " L");
            avgWaterLabel.setText(String.valueOf(round(calculator.calculateAverage(waterReadings))));
            minWaterLabel.setText(String.valueOf(round(calculator.calculateMinimum(waterReadings))));
            maxWaterLabel.setText(String.valueOf(round(calculator.calculateMaximum(waterReadings))));
        } else {
            totalWaterLabel.setText("Total Water: no data for this period");
            avgWaterLabel.setText("-");
            minWaterLabel.setText("-");
            maxWaterLabel.setText("-");
        }
    }

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

        // refreshes the consumption chart layout after the content becomes visible
        Platform.runLater(consumptionChart::requestLayout);
    }

    // shows the statistics tab content and hides everything else
    // currently defaults the utility toggle to 'energy'
    private void showStatistics() {
        hideAllTabContent();
        statisticsContentBox.setVisible(true);
        statisticsContentBox.setManaged(true);
        energyToggle.setSelected(true);

        updateStatisticsData();

        // refreshes the distribution chart layout after the content becomes visible
        Platform.runLater(distributionChart::requestLayout);
    }

    // loads readings for the selected utility and period and then updates the statistics and chart
    private void updateStatisticsData() {
        distributionChart.getData().clear();

        String resourceType;
        String unit;

        // determine which utility the user selected and set its measurement unit
        if (energyToggle.isSelected()) {
            resourceType = "energy";
            unit = "kWh";
        } else if (waterToggle.isSelected()) {
            resourceType = "water";
            unit = "L";
        } else {
            // if both utilities are selected, handle them separately as energy and water use different units
            updateStatisticsForBothUtilities();
            return;
        }

        // retrieve readings for the selected utility and convert them to quantity values
        List<Reading> readings = getReadings(resourceType);
        List<Double> values = readings.stream().map(Reading::getQuantity).toList();

        // calculate the total number of days in the selected period, including both start and end dates
        long totalDaysInPeriod = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;

        if (values.isEmpty()) {
            medianValueLabel.setText("No data");
            stdDevValueLabel.setText("-");
            mostCommonRangeValueLabel.setText("-");
            variabilityValueLabel.setText("-");
            daysRecordedValueLabel.setText("0 of " + totalDaysInPeriod);
            distributionChart.getData().clear();
            return;
        }

        // calculate the main statistics using the values from the selected utility
        double median = calculator.calculateMedian(values);
        double stdDev = calculator.calculateStandardDeviation(values);
        double average = calculator.calculateAverage(values);

        medianValueLabel.setText(round(median) + " " + unit);
        stdDevValueLabel.setText(round(stdDev) + " " + unit);

        // count the number of unique days on which readings were recorded
        long daysRecorded = readings.stream().map(reading -> reading.getDateTime().toLocalDate()).distinct().count();
        daysRecordedValueLabel.setText(daysRecorded + " of " + totalDaysInPeriod);

        // calculate the coefficient of variation to give a simple low/moderate/high variability rating
        double coefficientOfVariation = average == 0 ? 0 : (stdDev / average) * 100;
        String variability;
        if (coefficientOfVariation < 15) {
            variability = "Low";
        } else if (coefficientOfVariation < 35) {
            variability = "Moderate";
        } else {
            variability = "High";
        }
        variabilityValueLabel.setText(variability);

        // group the readings into ranges and display the most common range
        mostCommonRangeValueLabel.setText(updateDistributionChart(values, unit));
    }

    // calculates and displays statistics for both energy and water when 'both' is selected
    private void updateStatisticsForBothUtilities() {
        // retrieve energy and water readings separately so their statistics can be calculated using their own units
        List<Reading> energyReadingsList = getReadings("energy");
        List<Reading> waterReadingsList = getReadings("water");

        // extract the quantities from each utility's readings
        List<Double> energyValues = energyReadingsList.stream().map(Reading::getQuantity).toList();
        List<Double> waterValues = waterReadingsList.stream().map(Reading::getQuantity).toList();

        // calculate the total number of days in the selected period
        long totalDaysInPeriod = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;

        distributionChart.getData().clear();

        boolean hasEnergyData = !energyValues.isEmpty();
        boolean hasWaterData = !waterValues.isEmpty();

        // show on labels that there is no data if neither utility has readings
        if (!hasEnergyData && !hasWaterData) {
            medianValueLabel.setText("No data");
            stdDevValueLabel.setText("-");
            mostCommonRangeValueLabel.setText("-");
            variabilityValueLabel.setText("-");
            daysRecordedValueLabel.setText("0 of " + totalDaysInPeriod);
            return;
        }

        // calculate the median separately for each utility
        String energyMedianText = hasEnergyData ? "Energy: " + round(calculator.calculateMedian(energyValues)) + " kWh" : "Energy: no data";
        String waterMedianText = hasWaterData ? "Water: " + round(calculator.calculateMedian(waterValues)) + " L" : "Water: no data";
        medianValueLabel.setText(energyMedianText + ", " + waterMedianText);

        // calculate standard deviation separately for energy and water
        String energyStdDevText = hasEnergyData ? "Energy: " + round(calculator.calculateStandardDeviation(energyValues)) + " kWh" : "Energy: no data";
        String waterStdDevText = hasWaterData ? "Water: " + round(calculator.calculateStandardDeviation(waterValues)) + " L" : "Water: no data";
        stdDevValueLabel.setText(energyStdDevText + ", " + waterStdDevText);

        // count the unique days with readings for each utility
        long energyDaysRecorded = energyReadingsList.stream().map(r -> r.getDateTime().toLocalDate()).distinct().count();
        long waterDaysRecorded = waterReadingsList.stream().map(r -> r.getDateTime().toLocalDate()).distinct().count();
        daysRecordedValueLabel.setText("Energy: " + energyDaysRecorded + "/" + totalDaysInPeriod
                + ", Water: " + waterDaysRecorded + "/" + totalDaysInPeriod);

        // describe the variability of each utility's readings independently
        String energyVariabilityText = hasEnergyData ? "Energy: " + describeVariability(energyValues) : "Energy: no data";
        String waterVariabilityText = hasWaterData ? "Water: " + describeVariability(waterValues) : "Water: no data";
        variabilityValueLabel.setText(energyVariabilityText + ", " + waterVariabilityText);

        // display both utilities as separate series in the distribution chart so their readings show the correct measurement unit
        String energyMostCommonText = hasEnergyData ? "Energy: " + updateDistributionChart(energyValues, "kWh") : "Energy: no data";
        String waterMostCommonText = hasWaterData ? "Water: " + updateDistributionChart(waterValues, "L") : "Water: no data";
        mostCommonRangeValueLabel.setText(energyMostCommonText + ", " + waterMostCommonText);
    }

    // calculates a variability level from the coefficient of variation
    private String describeVariability(List<Double> values) {
        // calculate the average and standard deviation needed for the coefficient of variation
        double average = calculator.calculateAverage(values);
        double stdDev = calculator.calculateStandardDeviation(values);

        // express the standard deviation as a percentage of the average
        double coefficientOfVariation = average == 0 ? 0 : (stdDev / average) * 100;

        // convert the percentage into a simple variability category
        if (coefficientOfVariation < 15) return "Low";
        if (coefficientOfVariation < 35) return "Moderate";
        return "High";
    }

    // builds the consumption distribution chart from readings and returns the most common range
    private String updateDistributionChart(List<Double> readings, String unit) {
        distributionChart.getData().clear();

        // find the lowest and highest readings to determine the overall range
        double min = calculator.calculateMinimum(readings);
        double max = calculator.calculateMaximum(readings);
        List<DistributionCalculator.Range> ranges = distributionCalculator.computeRanges(readings, min, max);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // create a chart data point for each reading range and its total count
        for (DistributionCalculator.Range range : ranges) {
            series.getData().add(new XYChart.Data<>(String.format("%.0f-%.0f", range.start, range.end), range.count));
        }
        distributionChart.getData().add(series);

        // finds the range containing the most readings
        DistributionCalculator.Range mostCommonRange = distributionCalculator.findMostCommonRange(ranges);
        return String.format("%.0f-%.0f %s", mostCommonRange.start, mostCommonRange.end, unit);
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

