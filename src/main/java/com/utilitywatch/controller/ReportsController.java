package com.utilitywatch.controller;

import com.utilitywatch.reports.DistributionCalculator;
import com.utilitywatch.reports.ReportsDataService;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import javafx.scene.chart.BarChart;
import javafx.application.Platform;

import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.stream.IntStream;

import com.utilitywatch.Reading;
import com.utilitywatch.reports.ConsumptionSummaryCalculator;

public class ReportsController {
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // display content
    @FXML
    private VBox overviewContentBox;
    @FXML
    private VBox trendsContentBox;
    @FXML
    private VBox comparisonsContentBox;
    @FXML
    private Label comingSoonLabel;
    @FXML
    private VBox statisticsContentBox;
    @FXML
    private PeriodPickerController periodPickerController;
    // utility toggle buttons
    @FXML
    private HBox utilityToggle;

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

    // trend mode toggle buttons
    @FXML private HBox trendmodeToggle;

    @FXML private ToggleButton trendmodeTotalToggle;
    @FXML private ToggleButton trendmodeChangeToggle;
    @FXML private ComboBox<String> trendViewDropdown;
    @FXML private ComboBox<String> trendYearDropdown;
    @FXML private ComboBox<String> trendPeriodDropdown;

    private String trendPreviousViewSelection;

    @FXML private HBox trendEnergyMain;
    @FXML private HBox trendEnergyDisplay;
    @FXML private HBox trendWaterMain;
    @FXML private HBox trendWaterDisplay;

    @FXML private LineChart<String, Number> trendEnergyGraph;
    @FXML private LineChart<String, Number> trendWaterGraph;

    @FXML private CheckBox trendShowEnergyTrendButton;
    @FXML private CheckBox trendShowWaterTrendButton;

    // comparisons tab elements
    @FXML private Button comparisonPeriodDate1;
    @FXML private Button comparisonPeriodDate2;

    @FXML private BarChart<String, Number> comparisonTotalGraph;
    @FXML private BarChart<String, Number> comparisonAverageGraph;

    @FXML private Label comparisonTotalChangeEnergyLabel;
    @FXML private Label comparisonTotalChangeWaterLabel;
    @FXML private Label comparisonTotalPercentChangeEnergyLabel;
    @FXML private Label comparisonTotalPercentChangeWaterLabel;
    @FXML private Label comparisonAvgChangeEnergyLabel;
    @FXML private Label comparisonAvgChangeWaterLabel;
    @FXML private Label comparisonAvgPercentChangeEnergyLabel;
    @FXML private Label comparisonAvgPercentChangeWaterLabel;

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
    private final ReportsDataService dataService = new ReportsDataService();

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
        showTrends();
    }

    @FXML
    protected void onComparisonsTabClick() {
        showComparisons();
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


    // shows the trends tab content and hides everything else
    // Trend Mode: Total <Selected by Default>
    private void showTrends() {
        hideAllTabContent();
        utilityToggle.setVisible(false);
        utilityToggle.setManaged(false);

        trendmodeToggle.setVisible(true);
        trendmodeToggle.setManaged(true);

        trendsContentBox.setVisible(true);
        trendsContentBox.setManaged(true);
        trendmodeTotalToggle.setSelected(true);

        // Dropdown Initialization
        trendViewDropdown.setValue("Annual"); // Default Selection

        LocalDate periodStartLocal = LocalDate.of(1,1,1);
        LocalDate periodEndLocal = LocalDate.of(LocalDate.now().getYear(),12,31);

        // fixme: this is round about cause the there is no existing function in dataService to get all Readings for USER.
        // Get both Readings
        List<Reading> allEnergyReadings = dataService.getReadings("energy", periodStartLocal, periodEndLocal);
        List<Reading> allWaterReadings = dataService.getReadings("energy", periodStartLocal, periodEndLocal);

        // Merge List for both readings
        List<Reading> allUtilityReadings = new ArrayList<>(List.of());
        allUtilityReadings.addAll(allEnergyReadings);
        allUtilityReadings.addAll(allWaterReadings);

        // Get | Sort Years within and wrap unique for each year
        List<String> allYearsListed = allUtilityReadings.stream().map(reading -> reading.getDateTime().getYear()).distinct().sorted(Comparator.reverseOrder()).map(String::valueOf).toList();

        trendYearDropdown.getItems().setAll(allYearsListed);

        // Set to first option for Years
        trendYearDropdown.setValue(trendYearDropdown.getItems().getFirst());
    }

    @FXML
    private void onShowEnergyTrendClick() {
        boolean selected = trendShowEnergyTrendButton.isSelected();
        onShowAnyTrendClick(selected, trendEnergyMain, trendEnergyDisplay);
    }

    @FXML
    private void onShowWaterTrendClick() {
        boolean selected = trendShowWaterTrendButton.isSelected();
        onShowAnyTrendClick(selected, trendWaterMain, trendWaterDisplay);
    }

    private void onShowAnyTrendClick(Boolean selected, HBox UtilityMain, HBox UtilityDisplay) {
        UtilityDisplay.setVisible(selected);
        UtilityDisplay.setManaged(selected);
        if (selected) {
            UtilityMain.getStyleClass().remove("trends-tab-utility-inactive");
            UtilityMain.getStyleClass().add("trends-tab-utility-active");

        } else {
            UtilityMain.getStyleClass().remove("trends-tab-utility-active");
            UtilityMain.getStyleClass().add("trends-tab-utility-inactive");
        };

        updateTrendDisplays();
    }

    //todo: make a method for each: (SUM, AVERAGE, Data-Point)
    // to show the plot lines for each data sets and show the data dots

    @FXML
    private void onTrendsViewSelected() {
        String ViewSelected = trendViewDropdown.getValue();

        // Set Period-Options to corresponding View-Selected
        switch (ViewSelected) {
            case "Annual", "This Week" -> {
                trendPeriodDropdown.setPromptText("<No Options>");
            }
            case "Semester" -> {
                trendPeriodDropdown.getItems().setAll("1","2");
            }
            case "Trimester" -> {
                trendPeriodDropdown.getItems().setAll("1","2","3");
            }
            case "Quarter" -> {
                trendPeriodDropdown.getItems().setAll("1","2","3","4");
            }
            case "Month" -> {
                trendPeriodDropdown.getItems().setAll("January","February","March","April","May","June","July","August","September","October","November","December");
            }
            default -> {}
        }

        // Set Default Period Selection Value -> View-Selected
        // Update Graph
        trendPeriodDropdown.getSelectionModel().clearSelection();
        if (Objects.equals(ViewSelected,"Annual") || Objects.equals(ViewSelected,"This Week")) {
            trendPeriodDropdown.setValue(null);
            // Exception case for selecting between Annual and This Week. Both returns null value for Period
            // so App doesn't recognize the selection.
            if (!ViewSelected.equals(trendPreviousViewSelection)) {
                onTrendsPeriodSelected();
            }
        } else {
            trendPeriodDropdown.setValue(trendPeriodDropdown.getItems().getFirst());
        }

        trendPreviousViewSelection = ViewSelected;
    }

    @FXML
    private void onTrendsPeriodSelected() {
        updateTrendDisplays();
    }

    // Given the argument types, get data
    // it will return a TOTAL or AVERAGE output of all data readings within that DateTime (Period)
    private Number condenseTrendDataToParam(Object x_component, String utility) {
        int YearSelected = Integer.parseInt(trendYearDropdown.getValue());

        // Which way to condense reading data to (Default Months)
        // Switches to Days (D) for Month ViewSelected
        String ViewSelected = trendViewDropdown.getValue();

        LocalDate periodStartLocal = LocalDate.of(YearSelected,1,1);
        LocalDate periodEndLocal = LocalDate.of(YearSelected,12,31);

        String condenseType;
        switch (ViewSelected) {
            case "Month" -> condenseType = "D";
            case "This Week" -> {
                condenseType = "TW";
                periodStartLocal = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                periodEndLocal = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
            }
            default -> {condenseType = "M";}
        }

        List<Reading> selectedYearReadings = dataService.getReadings(utility, periodStartLocal, periodEndLocal);
        if (selectedYearReadings.isEmpty()) { return 0.0; }

        // Default
        List<Reading> condenseReadingsToDate = selectedYearReadings;

        switch (condenseType) {
            case "M" -> {
                Month monthEnum = Month.valueOf(x_component.toString().toUpperCase());
                condenseReadingsToDate = selectedYearReadings.stream().filter(reading -> reading.getDateTime().getMonth().equals(monthEnum)).toList();
            }
            case "TW" -> {
                DayOfWeek dayofweekEnum = DayOfWeek.valueOf(x_component.toString().toUpperCase());
                condenseReadingsToDate = selectedYearReadings.stream().filter(reading -> reading.getDateTime().getDayOfWeek().equals(dayofweekEnum)).toList();
            }
            case "D" -> {
                String PeriodSelected = trendPeriodDropdown.getValue();
                Month monthEnum = Month.valueOf(PeriodSelected.toUpperCase());
                int day = Integer.parseInt(x_component.toString());

                MonthDay monthDay = MonthDay.of(monthEnum, day);
                condenseReadingsToDate = selectedYearReadings.stream().filter(reading -> MonthDay.from(reading.getDateTime()).equals(monthDay)).toList();
            }
            default -> {}
        }
        if (condenseReadingsToDate.isEmpty()) { return 0.0; }

        double total = 0.0;
        for (Reading reading : condenseReadingsToDate) {
            total += reading.getQuantity();
        }

        return total; // Return condensed reading for param period
    }

    @FXML
    private void updateTrendDisplays () {
        String ViewSelected = trendViewDropdown.getValue();

        // No Options Selected
        if (trendYearDropdown.getValue() == null) { return; }

        int YearSelected = Integer.parseInt(trendYearDropdown.getValue());
        int MonthSelected;
        String PeriodSelected = trendPeriodDropdown.getValue();

        List<String> list_months = List.of("January","February","March","April","May","June","July","August","September","October","November","December");
        List<String> x_axis = List.of();

        // Set X-Axis Keys for Graph
        switch (ViewSelected) {
            case "Annual" -> {
                x_axis = list_months;
                //fixme: November in Annual doesn't show in the x_axis key
            }
            case "Semester" -> {
                if (PeriodSelected == null) { return; } //Ignore Spam Requests (from app)
                x_axis = new ArrayList<>(list_months.subList((list_months.size()/2) * (Integer.parseInt(PeriodSelected)-1), (list_months.size()/2) * (Integer.parseInt(PeriodSelected)))); // Seperate Months into Semester
            }
            case "Trimester" -> {
                if (PeriodSelected == null) { return; }
                x_axis = new ArrayList<>(list_months.subList((list_months.size()/3) * (Integer.parseInt(PeriodSelected)-1), (list_months.size()/3) * (Integer.parseInt(PeriodSelected))));
            }
            case "Quarter" -> {
                if (PeriodSelected == null) { return; }
                x_axis = new ArrayList<>(list_months.subList((list_months.size()/4) * (Integer.parseInt(PeriodSelected)-1), (list_months.size()/4) * (Integer.parseInt(PeriodSelected))));
            }
            case "Month" -> {
                if (PeriodSelected == null) { return; }
                MonthSelected = Month.valueOf(PeriodSelected.toUpperCase()).getValue();
                //fixme: debug, remove in final
                System.out.println("Month: " + MonthSelected);
                LocalDateTime DaysInMonth_Parsing;
                try {
                    DaysInMonth_Parsing = LocalDateTime.of(YearSelected, MonthSelected, 1, 0, 0, 0);
                } catch (Exception e) {
                    System.out.println("Exception: " + e);
                    return;
                }
                int DaysInMonth = DaysInMonth_Parsing.getMonth().length(DaysInMonth_Parsing.toLocalDate().isLeapYear());

                x_axis = IntStream.rangeClosed(1, DaysInMonth).boxed().map(String::valueOf).toList();
            }
            case "This Week" -> {
                x_axis = new ArrayList<>(List.of("Monday","Tuesday","Wednesday","Thursday","Friday","Saturday","Sunday"));
            }
            default -> {}
        }

        //fixme: debug, remove in final
//        System.out.println("View: " + ViewSelected + "\t\tYear: " + YearSelected + "\t\tPeriod: " + PeriodSelected);

        boolean selectedEnergy = trendShowEnergyTrendButton.isSelected();
        boolean selectedWater = trendShowWaterTrendButton.isSelected();

        // todo: can simplify both Energy and Water loops to a single method.
        // Add Energy Readings if Showing
        XYChart.Series<String, Number> EnergyGraph = new XYChart.Series<>();
        if (selectedEnergy) {
            EnergyGraph.setName("Energy Trend " + YearSelected);

            // Loop through x_axis (Date)
            for (String x_component : x_axis) {
                Number condensedReadingData = condenseTrendDataToParam(x_component, "energy");
                EnergyGraph.getData().add(new XYChart.Data<>(x_component, condensedReadingData));
            }
        }

        // Add Water Readings if Showing
        XYChart.Series<String, Number> WaterGraph = new XYChart.Series<>();
        if (selectedWater) {
            WaterGraph.setName("Water Trend " + YearSelected);

            // Loop through x_axis (Date)
            for (String x_component : x_axis) {
                Number condensedReadingData = condenseTrendDataToParam(x_component, "water");
                WaterGraph.getData().add(new XYChart.Data<>(x_component, condensedReadingData));
            }
        }

        // clean graph
        trendEnergyGraph.getData().clear();
        trendWaterGraph.getData().clear();

        // adds data series to the consumption chart
        trendEnergyGraph.getData().addAll(EnergyGraph);
        trendWaterGraph.getData().addAll(WaterGraph);

        //todo: add data for AVERAGE line

        Node EnergyLine = EnergyGraph.getNode().lookup(".trends-tab-utility-display .chart-series-line");
        EnergyLine.setStyle("-fx-stroke: #FFBA4A;");

        for (XYChart.Data<String, Number> datapoint : EnergyGraph.getData()) {
            Node node = datapoint.getNode();
            node.setStyle("-fx-background-color: #FFBA4A;");
        }

        Node WaterLine = WaterGraph.getNode().lookup(".trends-tab-utility-display .chart-series-line");
        WaterLine.setStyle("-fx-stroke: #5FB2F5;");

        for (XYChart.Data<String, Number> datapoint : WaterGraph.getData()) {
            Node node = datapoint.getNode();
            node.setStyle("-fx-background-color: #5FB2F5;");
        }
    }

    private void showComparisons() {
        hideAllTabContent();
        comparisonsContentBox.setVisible(true);
        comparisonsContentBox.setManaged(true);
        bothToggle.setSelected(true);

        //fixme: replace with updateComparisons(); after DB integration
        loadMockComparisonCharts();
    }

    //fixme: update comparison charts on period change
    //fixme: remove the top-right "periodButton" visual (irrelevant to tab)
    private void updateComparisons() {

        // Optional: Hide the legend if individual colors make it irrelevant
//        comparisonTotalGraph.setLegendVisible(false);
    }

    private void stylizeComparisonBar(XYChart.Data<String, Number> Bar, String Style) {
        Bar.nodeProperty().addListener((Observable, Old, New) -> {
            if (New != null) {
                New.setStyle(Style);
            }
        });
    }

    private void loadMockComparisonCharts() {
        //fixme: Test method for updateComparisons
        // for now it makes the mock graphs

        // Clear Both Graphs of Data
        comparisonTotalGraph.getData().clear();
        comparisonAverageGraph.getData().clear();

        // Make Mock Data for Total Graph
        // Energy Data
        XYChart.Series<String, Number> EnergyTotalSeries = new XYChart.Series<>();
        EnergyTotalSeries.setName("Energy");

        XYChart.Data<String, Number> energyTotalP1 = new XYChart.Data<>("Aug 1 - Sep 1, 2026", 2.5);
        XYChart.Data<String, Number> energyTotalDiff = new XYChart.Data<>("Difference", 1.5);
        XYChart.Data<String, Number> energyTotalP2 = new XYChart.Data<>("Oct 1 - Nov 1, 2026", 4);

        EnergyTotalSeries.getData().add(energyTotalP1);
        EnergyTotalSeries.getData().add(energyTotalDiff);
        EnergyTotalSeries.getData().add(energyTotalP2);

        // Water Data
        XYChart.Series<String, Number> WaterTotalSeries = new XYChart.Series<>();
        WaterTotalSeries.setName("Water");

        XYChart.Data<String, Number> waterTotalP1 = new XYChart.Data<>("Aug 1 - Sep 1, 2026", 2.5);
        XYChart.Data<String, Number> waterTotalDiff = new XYChart.Data<>("Difference", 1.5);
        XYChart.Data<String, Number> waterTotalP2 = new XYChart.Data<>("Oct 1 - Nov 1, 2026", 4);

        WaterTotalSeries.getData().add(waterTotalP1);
        WaterTotalSeries.getData().add(waterTotalDiff);
        WaterTotalSeries.getData().add(waterTotalP2);

        // Color Bars, Before Adding Data so Listener can Query (Except Null)
        stylizeComparisonBar(energyTotalP1,"-fx-bar-fill: #FFBA4A;");
        stylizeComparisonBar(energyTotalDiff,"-fx-bar-fill: #FFBA4A;");
        stylizeComparisonBar(energyTotalP2,"-fx-bar-fill: #FFBA4A;");

        stylizeComparisonBar(waterTotalP1,"-fx-bar-fill: #5FB2F5;");
        stylizeComparisonBar(waterTotalDiff,"-fx-bar-fill: #5FB2F5;");
        stylizeComparisonBar(waterTotalP2,"-fx-bar-fill: #5FB2F5;");

        // Add Both Datas to Graph
        comparisonTotalGraph.getData().addAll(EnergyTotalSeries, WaterTotalSeries);

        // Make Mock Data for Average Graph
        // Energy Data
        XYChart.Series<String, Number> EnergyAverageSeries = new XYChart.Series<>();
        EnergyAverageSeries.setName("Energy");

        XYChart.Data<String, Number> energyAverageP1 = new XYChart.Data<>("Aug 1 - Sep 1, 2026", 2.5);
        XYChart.Data<String, Number> energyAverageDiff = new XYChart.Data<>("Difference", 1.5);
        XYChart.Data<String, Number> energyAverageP2 = new XYChart.Data<>("Oct 1 - Nov 1, 2026", 4);

        EnergyAverageSeries.getData().add(energyAverageP1);
        EnergyAverageSeries.getData().add(energyAverageDiff);
        EnergyAverageSeries.getData().add(energyAverageP2);

        // Water Data
        XYChart.Series<String, Number> WaterAverageSeries = new XYChart.Series<>();
        WaterAverageSeries.setName("Water");

        XYChart.Data<String, Number> waterAverageP1 = new XYChart.Data<>("Aug 1 - Sep 1, 2026", 2.5);
        XYChart.Data<String, Number> waterAverageDiff = new XYChart.Data<>("Difference", 1.5);
        XYChart.Data<String, Number> waterAverageP2 = new XYChart.Data<>("Oct 1 - Nov 1, 2026", 4);

        WaterAverageSeries.getData().add(waterAverageP1);
        WaterAverageSeries.getData().add(waterAverageDiff);
        WaterAverageSeries.getData().add(waterAverageP2);

        // Color Bars, Before Adding Data so Listener can Query (Except Null)
        stylizeComparisonBar(energyAverageP1,"-fx-bar-fill: #FFBA4A;");
        stylizeComparisonBar(energyAverageDiff,"-fx-bar-fill: #FFBA4A;");
        stylizeComparisonBar(energyAverageP2,"-fx-bar-fill: #FFBA4A;");

        stylizeComparisonBar(waterAverageP1,"-fx-bar-fill: #5FB2F5;");
        stylizeComparisonBar(waterAverageDiff,"-fx-bar-fill: #5FB2F5;");
        stylizeComparisonBar(waterAverageP2,"-fx-bar-fill: #5FB2F5;");

        // Add Both Datas to Graph
        comparisonAverageGraph.getData().addAll(EnergyAverageSeries, WaterAverageSeries);
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

    // shows coming soon placeholder and hides everything else
    private void showComingSoon() {
        hideAllTabContent();
        comingSoonLabel.setVisible(true);
        comingSoonLabel.setManaged(true);
    }

    // hides all tab content and removes it from the layout
    // each show() method can then show only the needed content for that tab
    private void hideAllTabContent() {
        // The 3 Toggle Buttons are on by default, currently only in
        // Trends and Comparisons do they need to disappear
        utilityToggle.setVisible(true);
        utilityToggle.setManaged(true);

        trendmodeToggle.setVisible(false);
        trendmodeToggle.setManaged(false);

        trendsContentBox.setVisible(false);
        trendsContentBox.setManaged(false);

        comparisonsContentBox.setVisible(false);
        comparisonsContentBox.setManaged(false);

        overviewContentBox.setVisible(false);
        overviewContentBox.setManaged(false);

        statisticsContentBox.setVisible(false);
        statisticsContentBox.setManaged(false);

        comingSoonLabel.setVisible(false);
        comingSoonLabel.setManaged(false);
    }
}

