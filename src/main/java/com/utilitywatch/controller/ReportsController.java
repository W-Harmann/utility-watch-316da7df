package com.utilitywatch.controller;

import com.utilitywatch.consumptionDetector.UnusualConsumptionDetector;
import com.utilitywatch.reports.ConsumptionSummaryCalculator;
import com.utilitywatch.Reading;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;
import com.utilitywatch.reports.DistributionCalculator;
import com.utilitywatch.reports.ReportsDataService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import javafx.scene.chart.BarChart;
import javafx.application.Platform;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


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
    private VBox unusualPatternsContentBox;
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
    @FXML
    private LineChart<String, Number> unusualConsumptionChart;
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
    @FXML
    private Label selectedDateRange;
    @FXML
    private Label selectedUtilityUsed;
    @FXML
    private Label selectedUnusualReason;

    // reference to MainController for screen navigation
    private MainController mainController;

    private final ConsumptionSummaryCalculator calculator = new ConsumptionSummaryCalculator();
    private final DistributionCalculator distributionCalculator = new DistributionCalculator();
    private final ReportsDataService dataService = new ReportsDataService();

    private LinkedHashMap<List<Reading>, String> flaggedReadingsMap = new LinkedHashMap<>();
    private int selectedIndex = 0;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
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
            updateChartColour();
            if (statisticsContentBox.isVisible()) {
                updateStatisticsData();
            }
        });
        updateChartColour();

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

    // ---------- OVERVIEW TAB ----------

    // loads the user's energy and water readings for the selected period and then updates the chart and summary statistics
    private void updateOverviewData() {
        List<Double> energyReadings = dataService.getReadingQuantities("energy", periodStart, periodEnd);
        List<Double> waterReadings = dataService.getReadingQuantities("water", periodStart, periodEnd);

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

    // ---------- UNUSUAL PATTERNS TAB ----------

    // loads sample unusual consumption data
    private void loadUnusualConsumptionData() {
        // prevents duplicate data from being added to the chart
        if (!unusualConsumptionChart.getData().isEmpty()) {
            return;
        }
        User currentUser = Session.getInstance().getCurrentUser();
        int currentUserId = currentUser.getId();

        List<Reading> readings = new ArrayList<>(dataService.getReadings("energy", LocalDate.MIN, LocalDate.now()));

        // For the dates to be in ascending order
        readings.sort(Comparator.comparing(Reading::getDateTime));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        // creates the energy consumption data series and adds each energy value to its date
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        XYChart.Series<String, Number> flaggedSeries = new XYChart.Series<>();

        //TODO remove magic numbers

        ///ActiveHoursDetector activeHoursDetector = new ActiveHoursDetector(LocalTime.of(6,0), LocalTime.of(23, 0));
        //LongDurationDetector longDurationDetector = new LongDurationDetector(18.0, 72);
        ///SpikeDetector spikeDetector = new SpikeDetector(3.0, 3);
        UnusualConsumptionDetector unusualConsumptionDetector = new UnusualConsumptionDetector();

        boolean previousValueFlagged = false;
        LocalDateTime startOfWeekReading = null;


        List<Double> weeklyReadings = new ArrayList<>();
        List<Double> recordedWeeklyReadings = new ArrayList<>();


        List<Reading> flaggedReading;
        LinkedHashMap<List<Reading>, String> flaggedReadingsMap = new LinkedHashMap<>();


        for (Reading reading : readings) {
            if (startOfWeekReading == null) {
                startOfWeekReading = reading.getDateTime();
            }



            if (!recordedWeeklyReadings.isEmpty()) {
                if (unusualConsumptionDetector.isUnusual(reading.getQuantity(), recordedWeeklyReadings)) {
                    System.out.println("Flag triggered");
                    if (previousValueFlagged) {
                        System.out.println("Yes previous" + reading.getQuantity());
                        flaggedReadingsMap.sequencedKeySet().getLast().add(reading);
                    }
                    else {
                        flaggedReading = new ArrayList<>(List.of(reading));
                        flaggedReadingsMap.put(flaggedReading, "Differs from previous week");
                        previousValueFlagged = true;

                        System.out.println("Not previous" + reading.getQuantity());
                    }
                }
                else {
                    previousValueFlagged = false;
                }
            }

            if (reading.getDateTime().isAfter(startOfWeekReading.plusWeeks(1))) {

                startOfWeekReading = reading.getDateTime();

                recordedWeeklyReadings = weeklyReadings;

                double max = Collections.max(recordedWeeklyReadings);
                double min = Collections.min(recordedWeeklyReadings);

                System.out.println("MAX AND MIN:");
                System.out.println(max);
                System.out.println(min);

                weeklyReadings = new ArrayList<>();
            }

            System.out.println(reading.getQuantity());

            weeklyReadings.add(reading.getQuantity());

        }

        System.out.println("TOTAL FLAGGED READINGS:");

        this.flaggedReadingsMap = flaggedReadingsMap;

        for (List<Reading> flaggedReadings : flaggedReadingsMap.sequencedKeySet()) {
            System.out.println(flaggedReadings);
        }

        System.out.println("meow");

        showSelectedUnusualStats();


        // adds both data series to the consumption chart
        unusualConsumptionChart.getData().addAll(series, flaggedSeries);
    }

    // calculates and displays statistics for flagged reading
    private void showSelectedUnusualStats() {


        List<List<Reading>> flaggedReadingsList = new ArrayList<>(flaggedReadingsMap.sequencedKeySet());
        List<String> flaggedReadingsReasons = new ArrayList<>(flaggedReadingsMap.sequencedValues());

        List<Reading> flaggedReadings = flaggedReadingsList.get(selectedIndex);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd hh:mma");

        double totalUsage = 0;

        if (flaggedReadings.size() > 1) {
            selectedDateRange.setText("Selected: " + flaggedReadings.getFirst().getDateTime().format(formatter) + " - " + flaggedReadings.getLast().getDateTime().format(formatter));
        }
        else {
            selectedDateRange.setText("Selected: " + flaggedReadings.getFirst().getDateTime().format(formatter));
        }

        for (Reading reading : flaggedReadings) {
            totalUsage += reading.getQuantity();
        }

        selectedUtilityUsed.setText("Utility used: " + totalUsage + flaggedReadings.getFirst().getUnit());

        selectedUnusualReason.setText("Reason: " + flaggedReadingsReasons.get(selectedIndex));

    }


    // ---------- STATISTICS TAB ----------
    // stores the calculated statistics for one utility type, used for both single utility and combined utility reports
    private record UtilityStats(String medianText, String stdDevText, String daysRecordedText, String variabilityText, String mostCommonRangeText) {}

    // calculates all statistics for one utility type
    private UtilityStats computeStatsFor(String resourceType, String unit, long totalDaysInPeriod) {
        List<Reading> readings = dataService.getReadings(resourceType, periodStart, periodEnd);
        List<Double> values = readings.stream().map(Reading::getQuantity).toList();

        // returns default values when no readings are available
        if (values.isEmpty()) {
            return new UtilityStats("No data", "-", "0 of " + totalDaysInPeriod, "-", "-");
        }

        // calculates the median and standard deviation
        String median = round(calculator.calculateMedian(values)) + " " + unit;
        String stdDev = round(calculator.calculateStandardDeviation(values)) + " " + unit;

        // counts the number of days with recorded readings
        long daysRecorded = readings.stream().map(r -> r.getDateTime().toLocalDate()).distinct().count();

        // describes the reading variability and most common range
        String variability = calculator.describeVariability(values);
        String mostCommonRange = updateDistributionChart(values, unit);

        return new UtilityStats(median, stdDev, daysRecorded + " of " + totalDaysInPeriod, variability, mostCommonRange);
    }

    // loads readings for the selected utility and period and then updates the statistics and chart
    private void updateStatisticsData() {
        long totalDaysInPeriod = ChronoUnit.DAYS.between(periodStart, periodEnd) + 1;

        // shows separate statistics when both utilities are selected
        if (bothToggle.isSelected()) {
            showBothUtilityStats(totalDaysInPeriod);
        } else {
            // uses the selected utility and its matching unit
            String resourceType = energyToggle.isSelected() ? "energy" : "water";
            String unit = energyToggle.isSelected() ? "kWh" : "L";
            showSingleUtilityStats(resourceType, unit, totalDaysInPeriod);
        }
    }

    // calculates and displays statistics for one utility
    private void showSingleUtilityStats(String resourceType, String unit, long totalDaysInPeriod) {
        UtilityStats stats = computeStatsFor(resourceType, unit, totalDaysInPeriod);

        // updates each statistics label with the calculated values
        medianValueLabel.setText(stats.medianText());
        stdDevValueLabel.setText(stats.stdDevText());
        daysRecordedValueLabel.setText(stats.daysRecordedText());
        variabilityValueLabel.setText(stats.variabilityText());
        mostCommonRangeValueLabel.setText(stats.mostCommonRangeText());
    }

    // calculates statistics for energy and water and then displays both
    private void showBothUtilityStats(long totalDaysInPeriod) {
        UtilityStats energyStats = computeStatsFor("energy", "kWh", totalDaysInPeriod);
        UtilityStats waterStats = computeStatsFor("water", "L", totalDaysInPeriod);

        // displays each statistic for both utility types
        medianValueLabel.setText("Energy: " + energyStats.medianText() + ", Water: " + waterStats.medianText());
        stdDevValueLabel.setText("Energy: " + energyStats.stdDevText() + ", Water: " + waterStats.stdDevText());
        daysRecordedValueLabel.setText("Energy: " + energyStats.daysRecordedText() + ", Water: " + waterStats.daysRecordedText());
        variabilityValueLabel.setText("Energy: " + energyStats.variabilityText() + ", Water: " + waterStats.variabilityText());
        mostCommonRangeValueLabel.setText("Energy: " + energyStats.mostCommonRangeText() + ", Water: " + waterStats.mostCommonRangeText());
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

    // ---------- NAVIGATION ----------
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
    protected void onPreviousUnusualStatClick() {
        if (selectedIndex > 0) {
            selectedIndex--;
            showSelectedUnusualStats();
        }
    }

    @FXML
    protected void onNextUnusualStatClick() {
        if (selectedIndex < flaggedReadingsMap.size() - 1) {
            selectedIndex++;
            showSelectedUnusualStats();
        }
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

        updateStatisticsData();

        // refreshes the distribution chart layout after the content becomes visible
        Platform.runLater(distributionChart::requestLayout);
    }

    // updates the chart colour based on the selected utility toggle
    private void updateChartColour() {
        List<XYChart<String, Number>> allCharts = List.of(distributionChart, unusualConsumptionChart);

        for(XYChart<String, Number> chart: allCharts) {

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
