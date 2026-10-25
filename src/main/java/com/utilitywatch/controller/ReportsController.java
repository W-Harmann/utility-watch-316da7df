package com.utilitywatch.controller;

import com.utilitywatch.consumptionDetector.*;
import com.utilitywatch.reports.*;
import com.utilitywatch.Reading;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.time.*;
import javafx.application.Platform;
import javafx.util.StringConverter;

import java.time.temporal.ChronoUnit;
import java.util.stream.IntStream;

/**
 * Controller for the Reports screen ({@code report-view.fxml}). Shows the user analyses of the user's readings in the
 * form of an overview, trends, comparisons, unusual patterns, and statistics.
 */
public class ReportsController {
    private LocalDate periodStart;
    private LocalDate periodEnd;

    // display content
    @FXML private VBox overviewContentBox;
    @FXML private VBox trendsContentBox;
    @FXML private VBox comparisonsContentBox;
    @FXML private VBox unusualPatternsContentBox;
    @FXML private VBox statisticsContentBox;
    @FXML private Label comingSoonLabel;
    @FXML private PeriodPickerController periodPickerController;
    @FXML private GridPane overviewUnusualGrid;

    // utility toggle buttons
    @FXML private HBox utilityToggle;
    @FXML private ToggleButton energyToggle;
    @FXML private ToggleButton waterToggle;
    @FXML private ToggleButton bothToggle;
    @FXML private ToggleGroup utilityToggleGroup;

    // utility consumption charts
    @FXML private LineChart<String, Number> consumptionChart;
    @FXML private CategoryAxis consumptionXAxis;
    @FXML private BarChart<String, Number> distributionChart;
    @FXML private LineChart<Number, Number> unusualConsumptionChart;

    // trend mode toggle buttons
    @FXML private HBox trendmodeToggle;
    //    @FXML private ToggleButton trendmodeTotalToggle;
//    @FXML private ToggleButton trendmodeChangeToggle;
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
    @FXML private CheckBox trendShowEnergyTrendSumButton;
    @FXML private CheckBox trendShowEnergyTrendAvgButton;
    @FXML private CheckBox trendShowEnergyTrendDataPointButton;
    @FXML private CheckBox trendShowWaterTrendButton;
    @FXML private CheckBox trendShowWaterTrendSumButton;
    @FXML private CheckBox trendShowWaterTrendAvgButton;
    @FXML private CheckBox trendShowWaterTrendDataPointButton;

    // comparisons tab elements
    @FXML private HBox comparisonmodeToggle;
    @FXML private ComboBox<String> comparisonModeDropdown;

    @FXML private HBox comparisonsDatePickers;
    @FXML private HBox comparisonsDateViewer;

    @FXML private DatePicker fromDate1PickerComparison;
    @FXML private DatePicker toDate1PickerComparison;
    @FXML private DatePicker fromDate2PickerComparison;
    @FXML private DatePicker toDate2PickerComparison;

    @FXML private Label comparisonsDateTextFrom;
    @FXML private Label comparisonsDateTextTo;

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
    @FXML private Label totalEnergyLabel;
    @FXML private Label totalWaterLabel;
    @FXML private Label avgEnergyLabel;
    @FXML private Label avgWaterLabel;
    @FXML private Label minEnergyLabel;
    @FXML private Label minWaterLabel;
    @FXML private Label maxEnergyLabel;
    @FXML private Label maxWaterLabel;
    @FXML private Label medianValueLabel;
    @FXML private Label stdDevValueLabel;
    @FXML private Label mostCommonRangeValueLabel;
    @FXML private Label variabilityValueLabel;
    @FXML private Label daysRecordedValueLabel;
    // unusual patterns labels
    @FXML private Label selectedDateRange;
    @FXML private Label selectedUtilityUsed;
    @FXML private Label selectedUnusualReason;

    private MainController mainController;

    // reports overview tab maximum unusual patterns shown
    private static final int MAX_OVERVIEW_UNUSUAL_SHOWN = 3;

    private final ConsumptionSummaryCalculator calculator = new ConsumptionSummaryCalculator();
    private final DistributionCalculator distributionCalculator = new DistributionCalculator();
    private final ReportsDataService dataService = new ReportsDataService();

    /**
     * Gives this controller the {@link MainController}, so it can navigate to other screens. Called by
     * {@link MainController} when the screen is loaded.
     *
     * @param mainController the main controller that holds the navigation bar and content area
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    // rounds a value to one decimal place for display
    private double round(double value) {
        return Math.round(value * 10) / 10.0;
    }

    /**
     * Sets up the utility toggle and period picker listeners, defaults the comparison periods to the
     * last two months, and fills the overview tab. Called by the FXML loader.
     */
    @FXML
    public void initialize() {
        // updates the chart style whenever the utility toggle changes
        utilityToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            updateOverviewData();
            updateChartColour();
            if (statisticsContentBox.isVisible()) {
                updateStatisticsData();
            }
            if (unusualPatternsContentBox.isVisible()) {
                updateUnusualConsumptionData();
            }
        });
        updateChartColour();

        // period start and end for period date picker
        periodStart = periodPickerController.getStart();
        periodEnd = periodPickerController.getEnd();
        periodPickerController.setOnPeriodChanged(this::handlePeriodChanged);

        // comparisons tab defaults to the month before the main period
        toDate2PickerComparison.setValue(LocalDate.now());
        fromDate2PickerComparison.setValue(LocalDate.now().minusMonths(1));
        toDate1PickerComparison.setValue(fromDate2PickerComparison.getValue().minusMonths(1));
        fromDate1PickerComparison.setValue(toDate1PickerComparison.getValue().minusMonths(1));

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
        if (unusualPatternsContentBox.isVisible()) {
            updateUnusualConsumptionData();
        }
    }

    // ---------- OVERVIEW TAB ----------

    // loads the user's energy and water readings for the selected period and then updates the chart and summary statistics
    private void updateOverviewData() {
        // full readings are needed for the chart so each point can be labelled with its date
        List<Reading> energyReadings = dataService.getReadings("energy", periodStart, periodEnd);
        List<Reading> waterReadings = dataService.getReadings("water", periodStart, periodEnd);

        // the stat labels only need the quantities
        List<Double> energyValues = energyReadings.stream().map(Reading::getQuantity).toList();
        List<Double> waterValues = waterReadings.stream().map(Reading::getQuantity).toList();

        updateChart(energyReadings, waterReadings);
        updateStatLabels(energyValues, waterValues);
        updateOverviewUnusualTable();
    }

    // clears the chart and adds the selected energy and/or water readings as series
    private void updateChart(List<Reading> energyReadings, List<Reading> waterReadings) {
        // remove any existing data before displaying the updated readings
        consumptionChart.getData().clear();

        // check which resource toggles are currently selected
        boolean showEnergy = bothToggle.isSelected() || energyToggle.isSelected();
        boolean showWater = bothToggle.isSelected() || waterToggle.isSelected();

        List<Reading> energyToShow = showEnergy ? energyReadings : List.of();
        List<Reading> waterToShow = showWater ? waterReadings : List.of();

        // use the same label format for both series
        DateTimeFormatter labelFormat = chooseLabelFormat(energyToShow, waterToShow);

        // every date label from both series, oldest first, so the x axis is always in date order
        List<Reading> allShown = new ArrayList<>(energyToShow);
        allShown.addAll(waterToShow);
        allShown.sort(Comparator.comparing(Reading::getDateTime));
        Set<String> orderedLabels = new LinkedHashSet<>();
        for (Reading reading : allShown) {
            orderedLabels.add(reading.getDateTime().format(labelFormat));
        }

        // fixes the axis order instead of letting it follow the order that series were added
        consumptionXAxis.setAutoRanging(false);
        consumptionXAxis.setCategories(FXCollections.observableArrayList(orderedLabels));

        // add the energy and water readings to the chart if energy/water is selected and data is available
        if (showEnergy && !energyReadings.isEmpty()) {
            consumptionChart.getData().add(buildSeries("Energy (kWh)", energyReadings, labelFormat));
        }
        if (showWater && !waterReadings.isEmpty()) {
            consumptionChart.getData().add(buildSeries("Water (L)", waterReadings, labelFormat));
        }

        Platform.runLater(consumptionChart::requestLayout);
    }

    // uses date only, or date and time if any series has several readings on the same day
    // (category labels must be unique or points merge together)
    private DateTimeFormatter chooseLabelFormat(List<Reading> energyReadings, List<Reading> waterReadings) {
        boolean sharedDays = hasSeveralReadingsPerDay(energyReadings) || hasSeveralReadingsPerDay(waterReadings);
        return DateTimeFormatter.ofPattern(sharedDays ? "MMM d yyyy h:mma" : "MMM d yyyy");
    }

    private boolean hasSeveralReadingsPerDay(List<Reading> readings) {
        long distinctDays = readings.stream().map(reading -> reading.getDateTime().toLocalDate()).distinct().count();
        return distinctDays < readings.size();
    }

    // builds a series with one point per reading, sorted oldest first and labelled with the given format
    // (the same format as the x axis categories, so every point lines up with a category)
    private XYChart.Series<String, Number> buildSeries(String name, List<Reading> readings, DateTimeFormatter labelFormat) {
        List<Reading> sorted = new ArrayList<>(readings);
        sorted.sort(Comparator.comparing(Reading::getDateTime));

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName(name);
        for (Reading reading : sorted) {
            series.getData().add(new XYChart.Data<>(reading.getDateTime().format(labelFormat), reading.getQuantity()));
        }
        return series;
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

    // ---------- TRENDS TAB ----------
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
//        trendmodeTotalToggle.setSelected(true);

        // Dropdown Initialization
        trendViewDropdown.setValue("Annual"); // Default Selection

        LocalDate periodStartLocal = LocalDate.of(1,1,1);
        LocalDate periodEndLocal = LocalDate.of(LocalDate.now().getYear(),12,31);

        // fixme: this is round about cause the there is no existing function in dataService to get all Readings for USER.
        // Get both Readings
        List<Reading> allEnergyReadings = dataService.getReadings("energy", periodStartLocal, periodEndLocal);
        List<Reading> allWaterReadings = dataService.getReadings("water", periodStartLocal, periodEndLocal);

        // Merge List for both readings
        List<Reading> allUtilityReadings = new ArrayList<>(List.of());
        allUtilityReadings.addAll(allEnergyReadings);
        allUtilityReadings.addAll(allWaterReadings);

        // Get | Sort Years within and wrap unique for each year
        List<String> allYearsListed = allUtilityReadings.stream().map(reading -> reading.getDateTime().getYear()).distinct().sorted(Comparator.reverseOrder()).map(String::valueOf).toList();

        trendYearDropdown.getItems().setAll(allYearsListed);

        // Set to first option for Years (if there is any data)
        if (!trendYearDropdown.getItems().isEmpty()) {
            trendYearDropdown.setValue(trendYearDropdown.getItems().getFirst());
        }
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

            updateTrendDisplays();
        } else {
            UtilityMain.getStyleClass().remove("trends-tab-utility-active");
            UtilityMain.getStyleClass().add("trends-tab-utility-inactive");
        }
    }


    @FXML
    private void onTrendsViewSelected() {
        String ViewSelected = trendViewDropdown.getValue();

        // Set Period-Options to corresponding View-Selected
        switch (ViewSelected) {
            case "Annual", "This Week" -> {
                trendPeriodDropdown.setPromptText("<No Options>");
            }
            case "Semester" -> {
                trendPeriodDropdown.getItems().setAll("1", "2");
            }
            case "Trimester" -> {
                trendPeriodDropdown.getItems().setAll("1", "2", "3");
            }
            case "Quarter" -> {
                trendPeriodDropdown.getItems().setAll("1", "2", "3", "4");
            }
            case "Month" -> {
                trendPeriodDropdown.getItems().setAll("January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December");
            }
            default -> {
            }
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
    private Number condenseTrendDataToParam(Object x_component, String utility, String calculation_type) {
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

        switch (calculation_type) {
            case "sum" -> {
                return total; // Return condensed reading for param period as Summation
            }
            case "avg" -> {
                return total / condenseReadingsToDate.size(); // Return condensed reading for param period as Average
            }
            default -> {throw new IllegalStateException("Calculation Type for \"condenseTrendDataToParam()\" is invalid.");}
        }
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

        boolean selectedAvgEnergy = trendShowEnergyTrendAvgButton.isSelected();
        trendShowEnergyTrendSumButton.setSelected(!selectedAvgEnergy || trendShowEnergyTrendSumButton.isSelected()); // Always selected as default if nothing else.
        boolean selectedSumEnergy = trendShowEnergyTrendSumButton.isSelected();

        boolean selectedAvgWater = trendShowWaterTrendAvgButton.isSelected();
        trendShowWaterTrendSumButton.setSelected(!selectedAvgWater || trendShowWaterTrendSumButton.isSelected());
        boolean selectedSumWater = trendShowWaterTrendSumButton.isSelected();

        // todo: can simplify both Energy and Water loops to a single method.
        // fixme: can simplify loops that calculate the condensed data using modular methods
        // Add Energy Readings if Showing
        XYChart.Series<String, Number> EnergyGraphSum = new XYChart.Series<>();
        XYChart.Series<String, Number> EnergyGraphAvg = new XYChart.Series<>();
        if (selectedEnergy) {
            EnergyGraphSum.setName("Sum");
            EnergyGraphAvg.setName("Average");

            // Loop through x_axis (Date)
            for (String x_component : x_axis) {
                String selectedUtility = "energy";
                if (selectedSumEnergy) {
                    Number condensedReadingData = condenseTrendDataToParam(x_component, selectedUtility, "sum");
                    EnergyGraphSum.getData().add(new XYChart.Data<>(x_component, condensedReadingData));
                }
                if (selectedAvgEnergy) {
                    Number condensedReadingData = condenseTrendDataToParam(x_component, selectedUtility, "avg");
                    EnergyGraphAvg.getData().add(new XYChart.Data<>(x_component, condensedReadingData));
                }
            }
        }

        // Add Water Readings if Showing
        XYChart.Series<String, Number> WaterGraphSum = new XYChart.Series<>();
        XYChart.Series<String, Number> WaterGraphAvg = new XYChart.Series<>();
        if (selectedWater) {
            WaterGraphSum.setName("Sum");
            WaterGraphAvg.setName("Average");

            // Loop through x_axis (Date)
            for (String x_component : x_axis) {
                String selectedUtility = "water";
                if (selectedSumWater) {
                    Number condensedReadingData = condenseTrendDataToParam(x_component, selectedUtility, "sum");
                    WaterGraphSum.getData().add(new XYChart.Data<>(x_component, condensedReadingData));
                }
                if (selectedAvgWater) {
                    Number condensedReadingData = condenseTrendDataToParam(x_component, selectedUtility, "avg");
                    WaterGraphAvg.getData().add(new XYChart.Data<>(x_component, condensedReadingData));
                }
            }
        }

        // fixme: Can compact all of these lines into modular functions
        // clean graph
        trendEnergyGraph.getData().clear();
        trendWaterGraph.getData().clear();

        // show graph legends
        trendEnergyGraph.setLegendVisible(true);
        trendWaterGraph.setLegendVisible(true);

        // name graphs accordingly

        String graphName = Integer.toString(YearSelected);
        switch (ViewSelected) {
            case "Annual" -> {
                graphName = Integer.toString(YearSelected);
            }
            case "Semester" -> {
                graphName = "S" + PeriodSelected + " " + YearSelected;
            }
            case "Trimester" -> {
                graphName = "T" + PeriodSelected + " " + YearSelected;
            }
            case "Quarter" -> {
                graphName = "Q" + PeriodSelected + " " + YearSelected;
            }
            case "Month" -> {
                graphName = PeriodSelected + ", " + YearSelected;
            }
            case "This Week" -> {
                graphName = "This Week";
            }
            default -> {}
        }

        trendEnergyGraph.setTitle("(kWh) Energy Trend of: " + graphName);
        trendWaterGraph.setTitle("(L) Water Trend of: " + graphName);

        // adds data series to the consumption chart
        trendEnergyGraph.getData().addAll(EnergyGraphSum, EnergyGraphAvg);
        trendWaterGraph.getData().addAll(WaterGraphSum, WaterGraphAvg);

        // fixme: tidy up code \/
        //  The legends for the graph show the wrong color for Sum and Avg, between Energy and Water

        // Graph Stylizing
        // Energy
        // Sum
        stylizedTrendGraphSeries(EnergyGraphSum, "-fx-stroke: #FFBA4A;", "-fx-background-color: #FFBA4A;", trendShowEnergyTrendDataPointButton.isSelected());
        // Average
        stylizedTrendGraphSeries(EnergyGraphAvg, "-fx-stroke: #FF0000;", "-fx-background-color: #FF0000;", trendShowEnergyTrendDataPointButton.isSelected());
        // Water
        // Sum
        stylizedTrendGraphSeries(WaterGraphSum, "-fx-stroke: #5FB2F5;", "-fx-background-color: #5FB2F5;", trendShowWaterTrendDataPointButton.isSelected());
        // Average
        stylizedTrendGraphSeries(WaterGraphAvg, "-fx-stroke: #FF0000;", "-fx-background-color: #FF0000;", trendShowWaterTrendDataPointButton.isSelected());
    }

    private void stylizedTrendGraphSeries(XYChart.Series<String, Number> Series, String StyleLine, String StyleNode, boolean showDataPoint) {
        Node Line = Series.getNode().lookup(".trends-tab-utility-display .chart-series-line");
        Line.setStyle(StyleLine);

        for (XYChart.Data<String, Number> datapoint : Series.getData()) {
            Node node = datapoint.getNode();
            node.setStyle(StyleNode);

//            System.out.println(datapoint.getYValue().doubleValue() == 0.0);
            if (datapoint.getYValue().doubleValue() == 0.0 || !showDataPoint) { continue; }

            Runnable customizeNode = () -> {
                if (node instanceof StackPane pane) {

                    // Remove the node visually if it's in the way
                    pane.setStyle("-fx-background-color: WHITE; -fx-background-radius: 90px;");

                    Label label = new Label(String.valueOf(datapoint.getYValue()));
                    label.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #333333;");
                    // fixme: remove if obsolete
                    label.setTranslateY(0);

                    // Add the label
                    if (!pane.getChildren().contains(label)) {
                        pane.getChildren().add(label);
                    }
                }
            };

            // Draw the node if it exists
            if (datapoint.getNode() != null) {
                customizeNode.run();
            } else {
                // or wait for it to be drawn
                datapoint.nodeProperty().addListener((obs, oldNode, newNode) -> {
                    if (newNode != null) {
                        customizeNode.run();
                    }
                });
            }
        }
    }

    // ---------- COMPARISONS TAB ----------
    private void showComparisons() {
        hideAllTabContent();
        utilityToggle.setVisible(false);
        utilityToggle.setManaged(false);

        comparisonmodeToggle.setVisible(true);
        comparisonmodeToggle.setManaged(true);

        comparisonsContentBox.setVisible(true);
        comparisonsContentBox.setManaged(true);

        comparisonModeDropdown.setValue("Free Choice"); // Default Selection

        updateComparisons();
    }

    private void setComparisonsDateViewing(String from, String to) {
        comparisonsDateTextFrom.setText(from);
        comparisonsDateTextTo.setText(to);
    }

    @FXML
    private void onComparisonsViewSelected() {
        String mode = comparisonModeDropdown.getValue();

        comparisonsDatePickers.setVisible(mode.equals("Free Choice"));
        comparisonsDatePickers.setManaged(mode.equals("Free Choice"));

        comparisonsDateViewer.setVisible(!mode.equals("Free Choice"));
        comparisonsDateViewer.setManaged(!mode.equals("Free Choice"));

        LocalDate fromDate1 = null, toDate1 = null, fromDate2 = null, toDate2 = null;
        switch (mode) {
            case "Yesterday" -> {
                setComparisonsDateViewing("Yesterday","Today");

                fromDate1 = LocalDate.now().minusDays(1);
                toDate1 = LocalDate.now().minusDays(1);
                fromDate2 = LocalDate.now();
                toDate2 = LocalDate.now();
            }
            case "Last Week" -> {
                setComparisonsDateViewing("Last Week","This Week");

                fromDate2 = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                toDate2 = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
                fromDate1 = fromDate2.minusWeeks(1);
                toDate1 = toDate2.minusWeeks(1);
            }
            case "Last Month" -> {
                setComparisonsDateViewing("Last Month","This Month");

                fromDate2 = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth());
                toDate2 = LocalDate.now().with(TemporalAdjusters.lastDayOfMonth());
                fromDate1 = fromDate2.minusMonths(1);
                toDate1 = toDate2.minusMonths(1);
            }
            case "Last Year" -> {
                setComparisonsDateViewing("Last Year","This Year");

                fromDate2 = LocalDate.now().with(TemporalAdjusters.firstDayOfYear());
                toDate2 = LocalDate.now().with(TemporalAdjusters.lastDayOfYear());
                fromDate1 = fromDate2.minusYears(1);
                toDate1 = toDate2.minusYears(1);
            }
            default -> {
                updateComparisons();
                return;
            }
        }

        updateComparisons(fromDate1, toDate1, fromDate2, toDate2);
    }

    // todo: Trends page has a similar function, maybe these two can be modulated.
    // Given the argument types, get data
    // it will return a TOTAL or AVERAGE output of all data readings within that DateTime (Period)
    private double condenseComparisonDataToParam(LocalDate fromDate, LocalDate toDate, String utility, String calculation_type) {
        // Get readings from the specified date
        List<Reading> selectedPeriodReadings = dataService.getReadings(utility, fromDate, toDate);

        // Return if empty
        if (selectedPeriodReadings.isEmpty()) { return 0.0; }

        double total = 0.0;
        for (Reading reading : selectedPeriodReadings) {
            total += reading.getQuantity();
        }

        switch (calculation_type) {
            case "sum" -> {
                return total; // Return condensed reading for param period as Summation
            }
            case "avg" -> {
                return total / selectedPeriodReadings.size(); // Return condensed reading for param period as Average
            }
            default -> {throw new IllegalStateException("Calculation Type for \"condenseTrendDataToParam()\" is invalid.");}
        }
    }

    @FXML
    private void updateComparisons() {
        LocalDate fromDate1 = fromDate1PickerComparison.getValue();
        LocalDate toDate1 = toDate1PickerComparison.getValue();
        LocalDate fromDate2 = fromDate2PickerComparison.getValue();
        LocalDate toDate2 = toDate2PickerComparison.getValue();
        updateComparisons(fromDate1, toDate1, fromDate2, toDate2);
    }

    // fixme: kinda hard to navigate
    //  legend is also styled bad (text should not be white on white)
    @FXML
    private void updateComparisons(LocalDate fromDate1, LocalDate toDate1, LocalDate fromDate2, LocalDate toDate2) {
        // both comparison dates are needed
//        if (fromDate2 == null || toDate2 == null) {
//            return;
//        }

//        System.out.println(fromDate1);
//        System.out.println(toDate1);
//        System.out.println(fromDate2);
//        System.out.println(toDate2);

        // Clear Both Graphs of Data
        comparisonTotalGraph.getData().clear();
        comparisonAverageGraph.getData().clear();

        XYChart.Series<String, Number> EnergyTotalSeries = new XYChart.Series<>();
        EnergyTotalSeries.setName("Energy");

        XYChart.Series<String, Number> WaterTotalSeries = new XYChart.Series<>();
        WaterTotalSeries.setName("Water");

        XYChart.Series<String, Number> EnergyAverageSeries = new XYChart.Series<>();
        EnergyAverageSeries.setName("Energy");

        XYChart.Series<String, Number> WaterAverageSeries = new XYChart.Series<>();
        WaterAverageSeries.setName("Water");

        // Formatting the Date to desired format
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MM/dd/yyyy");

        String fromDate1String = fromDate1.format(format);
        String toDate1String = toDate1.format(format);
        String fromDate2String = fromDate2.format(format);
        String toDate2String = toDate2.format(format);

        String DisplayPeriod1 = fromDate1String + " - " + toDate1String;
        String DisplayPeriod2 = fromDate2String + " - " + toDate2String;

        class ComparisonHandler {
            final XYChart.Series<String, Number> series;
            final double dataPeriod1, dataPeriod2;

            /**
             * Works out the two periods' values, fills the change and percentage change labels, and adds
             * styled bars for both periods and their difference to the series.
             *
             * @param series the chart series to add the bars to
             * @param AssociatedLabel the label showing the change between the periods
             * @param AssociatedLabelPercent the label showing the percentage change
             * @param Style the CSS style applied to the bars, e.g. {@code "-fx-bar-fill: #FFBA4A;"}
             * @param Utility {@code "energy"} or {@code "water"}
             * @param CalculationType how readings are combined, {@code "sum"} for the total or {@code "avg"} for the average
             * @param fromDate1 the first day of the first period
             * @param toDate1 the last day of the first period
             * @param fromDate2 the first day of the second period
             * @param toDate2 the last day of the second period
             */
            public ComparisonHandler(XYChart.Series<String, Number> series, Label AssociatedLabel, Label AssociatedLabelPercent, String Style, String Utility, String CalculationType, LocalDate fromDate1, LocalDate toDate1, LocalDate fromDate2, LocalDate toDate2) {
                this.series = series;

                // Get condensed data of period range
                dataPeriod1 = condenseComparisonDataToParam(fromDate1, toDate1, Utility, CalculationType);
                dataPeriod2 = condenseComparisonDataToParam(fromDate2, toDate2, Utility, CalculationType);

                // set Labels
                String unit = Utility.equals("water") ? "L" : "kWh";
                AssociatedLabel.setText(Utility.substring(0,1).toUpperCase(Locale.ROOT) + Utility.substring(1) + ": " + round(difference()) + " " + unit);

                double percentChangeData = percentChange(2);

                // If Infinity is detected, set N/a instead.
                if (!Double.isFinite(percentChangeData)) {
                    AssociatedLabelPercent.setText("N/a");
                } else {
                    AssociatedLabelPercent.setText(((percentChangeData < 0) ? "-" : "+") + Math.abs(percentChangeData) + "%");
                }

                // set bar data
                XYChart.Data<String, Number> Period1 = new XYChart.Data<>(DisplayPeriod1, dataPeriod1);
                XYChart.Data<String, Number> Difference = new XYChart.Data<>("Difference", Math.abs(difference()));
                XYChart.Data<String, Number> Period2 = new XYChart.Data<>(DisplayPeriod2, dataPeriod2);

                // add to Series
                series.getData().add(Period1);
                series.getData().add(Difference);
                series.getData().add(Period2);

                // Color Bars, Before Adding Data to Bar Graph so Listener can Query (Except Null)
                stylizeComparisonBar(Period1,Style);
                stylizeComparisonBar(Difference,Style);
                stylizeComparisonBar(Period2,Style);
            }

            // Calculates the difference between the two periods
            double difference() {
                return dataPeriod2 - dataPeriod1;
            }

            // Calculates the percentage change between the two periods and round them to NUM decimal points
            double percentChange(int DecimalRound) {
                double percentage = (difference() / dataPeriod1) * 100;
                if (!Double.isFinite(percentage)) {
                    return percentage; // period 1 was 0, so there's no meaningful % change
                }
                DecimalRound = (int) (1 * Math.pow(10, DecimalRound));

                return (double) Math.round(percentage * DecimalRound) /DecimalRound;
            }
        }

        // fixme: Can be cleaner
        // Total Graphs
        // Energy Data
        ComparisonHandler EnergyTotal = new ComparisonHandler(
                EnergyTotalSeries,
                comparisonTotalChangeEnergyLabel,
                comparisonTotalPercentChangeEnergyLabel,
                "-fx-bar-fill: #FFBA4A;",
                "energy",
                "sum",
                fromDate1, toDate1, fromDate2, toDate2
        );

        // Water Data
        ComparisonHandler WaterTotal = new ComparisonHandler(
                WaterTotalSeries,
                comparisonTotalChangeWaterLabel,
                comparisonTotalPercentChangeWaterLabel,
                "-fx-bar-fill: #5FB2F5;",
                "water",
                "sum",
                fromDate1, toDate1, fromDate2, toDate2
        );

        // Add Both Datas to Graph
        comparisonTotalGraph.getData().addAll(EnergyTotal.series, WaterTotal.series);

        // Average Graph
        // Energy Data
        ComparisonHandler EnergyAvg = new ComparisonHandler(
                EnergyAverageSeries,
                comparisonAvgChangeEnergyLabel,
                comparisonAvgPercentChangeEnergyLabel,
                "-fx-bar-fill: #FFBA4A;",
                "energy",
                "avg",
                fromDate1, toDate1, fromDate2, toDate2
        );

        // Water Data
        ComparisonHandler WaterAvg = new ComparisonHandler(
                WaterAverageSeries,
                comparisonAvgChangeWaterLabel,
                comparisonAvgPercentChangeWaterLabel,
                "-fx-bar-fill: #5FB2F5;",
                "water",
                "avg",
                fromDate1, toDate1, fromDate2, toDate2
        );

        // Add Both Datas to Graph
        comparisonAverageGraph.getData().addAll(EnergyAvg.series, WaterAvg.series);

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



    // ---------- UNUSUAL PATTERNS TAB ----------

    private LinkedHashMap<List<Reading>, String> flaggedReadingsMap = new LinkedHashMap<>();
    private int selectedIndex = 0;

    // loads sample unusual consumption data
    private LinkedHashMap<List<Reading>, String> updateUnusualConsumptionData() {
        // prevents duplicate data from being added to the chart
        unusualConsumptionChart.getData().clear();

        NumberAxis xAxis = (NumberAxis) unusualConsumptionChart.getXAxis();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd");

        xAxis.setTickLabelFormatter(new StringConverter<Number>() {
            /**
             * Formats an axis value as a date label such as "Jan 05".
             *
             * @param epochMilliSeconds the axis value, in milliseconds since the epoch
             * @return the date label
             */
            @Override
            public String toString(Number epochMilliSeconds) {
                LocalDateTime dateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilliSeconds.longValue()), ZoneId.systemDefault());
                return dateTime.format(formatter);
            }

            /**
             * Not used: the axis only needs to turn values into labels.
             *
             * @param string the label to convert
             * @return always {@code null}
             */
            @Override
            public Number fromString(String string) {
                return null;
            }
        });

        // check which resource toggles are currently selected
        boolean showEnergy = bothToggle.isSelected() || energyToggle.isSelected();
        boolean showWater = bothToggle.isSelected() || waterToggle.isSelected();



        List<Reading> energyReadings = new ArrayList<>(dataService.getReadings("energy", periodStart, periodEnd));
        List<Reading> waterReadings = new ArrayList<>(dataService.getReadings("water", periodStart, periodEnd));

        LinkedHashMap<List<Reading>, String> utilityFlags = new LinkedHashMap<>();

        if (showEnergy && !energyReadings.isEmpty()) {
            // For the dates to be in ascending order
            energyReadings.sort(Comparator.comparing(Reading::getDateTime));

            LinkedHashMap<List<Reading>, String> energyFlags = loadUnusualConsumptionData(energyReadings);
            utilityFlags.putAll(energyFlags);
        }



        if (showWater && !waterReadings.isEmpty()) {
            waterReadings.sort(Comparator.comparing(Reading::getDateTime));

            LinkedHashMap<List<Reading>, String> waterFlags = loadUnusualConsumptionData(waterReadings);
            utilityFlags.putAll(waterFlags);
        }



        this.flaggedReadingsMap = sortFlaggedReadings(utilityFlags);

        selectedIndex = 0;

        updateUnusualChart(energyReadings, waterReadings);

        showSelectedUnusualStats();

        return this.flaggedReadingsMap;

    }

    // fills the overview's unusual patterns table with the most recent flagged readings
    private void updateOverviewUnusualTable() {
        // removes old rows but keeps the heading row (row 0)
        overviewUnusualGrid.getChildren().removeIf(node -> {
            Integer row = GridPane.getRowIndex(node);
            return row != null && row > 0;
        });

        List<Map.Entry<List<Reading>, String>> flagged = new ArrayList<>(updateUnusualConsumptionData().entrySet());

        if (flagged.isEmpty()) {
            overviewUnusualGrid.add(new Label("No unusual patterns found for this period."), 0, 1, 3, 1);
            return;
        }

        DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("MMM d h:mma");
        int rowsToShow = Math.min(flagged.size(), MAX_OVERVIEW_UNUSUAL_SHOWN);

        // flagged is oldest first, so read from the end to show the newest first
        for (int row = 0; row < rowsToShow; row++) {
            Map.Entry<List<Reading>, String> entry = flagged.get(flagged.size() - 1 - row);
            List<Reading> readings = entry.getKey();

            String when = readings.getFirst().getDateTime().format(timeFormat);
            if (readings.size() > 1) {
                when += " - " + readings.getLast().getDateTime().format(timeFormat);
            }

            double totalUsage = readings.stream().mapToDouble(Reading::getQuantity).sum();
            String used = String.format("%.1f %s", totalUsage, readings.getFirst().getUnit());

            Label reasonLabel = new Label(entry.getValue());
            reasonLabel.setWrapText(true);

            Label whenLabel = new Label(when);
            whenLabel.setWrapText(true);
            whenLabel.setMaxWidth(150.0); // wraps instead of running off the column
            overviewUnusualGrid.add(whenLabel, 0, row + 1);
            overviewUnusualGrid.add(reasonLabel, 1, row + 1);
            overviewUnusualGrid.add(new Label(used), 2, row + 1);
        }
    }

    /**
     * loads unusual consumption data
     * @param readings a list of readings used to find unusual readings
     * @return a map of readings that have been flagged as unusual alongside the reason as a string
     */
    public static LinkedHashMap<List<Reading>, String> loadUnusualConsumptionData(List<Reading> readings) {

        // For the dates to be in ascending order
        readings.sort(Comparator.comparing(Reading::getDateTime));

        ActiveHoursDetector activeHoursDetector = new ActiveHoursDetector(LocalTime.of(6,0), LocalTime.of(0, 0));
        LongDurationDetector longDurationDetector = new LongDurationDetector(15.0, 72);
        SpikeDetector spikeDetector = new SpikeDetector(1.3);
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
                    if (previousValueFlagged) {
                        flaggedReadingsMap.sequencedKeySet().getLast().add(reading);
                    }
                    else {
                        flaggedReading = new ArrayList<>(List.of(reading));
                        flaggedReadingsMap.put(flaggedReading, "Differs from previous week");
                        previousValueFlagged = true;
                    }
                }
                else {
                    previousValueFlagged = false;
                }
            }

            if (reading.getDateTime().isAfter(startOfWeekReading.plusWeeks(1))) {

                startOfWeekReading = reading.getDateTime();

                recordedWeeklyReadings = weeklyReadings;

                weeklyReadings = new ArrayList<>();
            }

            weeklyReadings.add(reading.getQuantity());

        }

        List<List<Reading>> flaggedLongDuration;

        flaggedLongDuration = longDurationDetector.hasLongDurationUsage(readings);

        for (List<Reading> reading : flaggedLongDuration) {
            flaggedReadingsMap.put(reading, "Consistently high activity");
        }


        List<Reading> spikes;

        spikes = spikeDetector.hasSpikes(readings);

        for (Reading reading : spikes) {
            List<Reading> spikedReading = new ArrayList<>(List.of(reading));
            flaggedReadingsMap.put(spikedReading, "Spike");
        }



        previousValueFlagged = false;

        for (Reading reading : readings) {

            if (activeHoursDetector.isUnusual(reading, 1)) {
                if (previousValueFlagged) {
                    flaggedReadingsMap.sequencedKeySet().getLast().add(reading);
                }
                else {
                    flaggedReading = new ArrayList<>(List.of(reading));
                    flaggedReadingsMap.put(flaggedReading, "Activity outside of active hours");
                    previousValueFlagged = true;
                }
            }
            else {
                previousValueFlagged = false;
            }
        }

        return flaggedReadingsMap;
    }

    private LinkedHashMap<List<Reading>, String> sortFlaggedReadings(LinkedHashMap<List<Reading>, String> utilityFlags) {

        // orders flagged reading map
        LinkedHashMap<List<Reading>, String> sortedFlaggedReadings = new LinkedHashMap<>();

        utilityFlags.entrySet().stream()
                .sorted(Map.Entry.comparingByKey(Comparator.comparing(list ->
                        list.stream()
                                .map(Reading::getDateTime)
                                .max(LocalDateTime::compareTo)
                                .orElse(LocalDateTime.MAX)
                )))
                .forEachOrdered(entry -> sortedFlaggedReadings.put(entry.getKey(), entry.getValue()));

        return sortedFlaggedReadings;
    }

    private void updateUnusualChart(List<Reading> energyReadings, List<Reading> waterReadings) {
        // remove any existing data before displaying the updated readings
        unusualConsumptionChart.getData().clear();

        // check which resource toggles are currently selected
        boolean showEnergy = bothToggle.isSelected() || energyToggle.isSelected();
        boolean showWater = bothToggle.isSelected() || waterToggle.isSelected();

        // add the energy readings to the chart if energy is selected and data is available
        XYChart.Series<Number, Number> energySeries = new XYChart.Series<>();
        energySeries.setName("Energy (kWh)");

        if (showEnergy && !energyReadings.isEmpty()) {
            // add each energy reading as a separate data point
            for (Reading reading : energyReadings) {
                energySeries.getData().add(new XYChart.Data<>(reading.getDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), reading.getQuantity()));
            }
            unusualConsumptionChart.getData().add(energySeries);
            energySeries.getData().forEach(data -> {
                data.getNode().setStyle("-fx-stroke: #FFBA4A; -fx-background-color: #FFBA4A, white;");
            });
        }
        else {
            unusualConsumptionChart.getData().add(energySeries);
        }

        // add the water readings to the chart if water is selected and data is available
        XYChart.Series<Number, Number> waterSeries = new XYChart.Series<>();
        waterSeries.setName("Water (L)");

        if (showWater && !waterReadings.isEmpty()) {
            // add each water reading as a separate data point
            for (Reading reading : waterReadings) {
                waterSeries.getData().add(new XYChart.Data<>(reading.getDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), reading.getQuantity()));
            }
            unusualConsumptionChart.getData().add(waterSeries);
            waterSeries.getData().forEach(data -> {
                data.getNode().setStyle("-fx-stroke: #5FB2F5; -fx-background-color: #5FB2F5, white;");
            });
        }
        else {
            unusualConsumptionChart.getData().add(waterSeries);
        }

        List<List<Reading>> flaggedReadingsList = new ArrayList<>(flaggedReadingsMap.sequencedKeySet());

        if(!flaggedReadingsList.isEmpty()) {
            for (int i = 0; i < flaggedReadingsList.size(); i++) {

                XYChart.Series<Number, Number> flaggedSeries = new XYChart.Series<>();
                for (Reading reading : flaggedReadingsList.get(i)) {
                    flaggedSeries.getData().add(new XYChart.Data<>(reading.getDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(), reading.getQuantity()));
                }
                unusualConsumptionChart.getData().add(flaggedSeries);
                if (i == selectedIndex) {
                    flaggedSeries.getData().forEach(data -> {
                        data.getNode().setStyle("-fx-stroke: #CB0E11; -fx-background-color: #CB0E11, #CB0E11;");
                    });
                } else {
                    flaggedSeries.getData().forEach(data -> {
                        data.getNode().setStyle("-fx-stroke: #CB0E11; -fx-background-color: #CB0E11, white;");
                    });
                }
            }
        }

        updateUnusualFlaggedChart(selectedIndex, selectedIndex);

    }

    private void updateUnusualFlaggedChart(int selected, int prevSelected) {
        List<List<Reading>> flaggedReadingsList = new ArrayList<>(flaggedReadingsMap.sequencedKeySet());

        NumberAxis xAxis = (NumberAxis) unusualConsumptionChart.getXAxis();

        if(!flaggedReadingsList.isEmpty()) {
            List<XYChart.Series<Number, Number>> flaggedSeries = unusualConsumptionChart.getData()
                    .stream()
                    .filter(series -> series.getName() == null)
                    .toList();

            flaggedSeries.get(prevSelected).getData().forEach(data -> {
                data.getNode().setStyle("-fx-background-color: #CB0E11, white");
            });

            flaggedSeries.get(selected).getData().forEach(data -> {
                data.getNode().setStyle("-fx-background-color: #CB0E11, #CB0E11;");
                data.getNode().toFront();
            });

            long monthRange = 30L * 24 * 60 * 60 * 1000;
            long tickUnit = monthRange / 30;

            xAxis.setAutoRanging(false);

            xAxis.setLowerBound((long) flaggedSeries.get(selected).getData().getFirst().getXValue() - monthRange / 2);
            xAxis.setUpperBound((long) flaggedSeries.get(selected).getData().getLast().getXValue() + monthRange / 2);
            xAxis.setTickUnit(tickUnit);

        }
        else {
            xAxis.setAutoRanging(true);
        }
    }

    // calculates and displays statistics for flagged reading
    private void showSelectedUnusualStats() {

        if (!flaggedReadingsMap.isEmpty()) {
            List<List<Reading>> flaggedReadingsList = new ArrayList<>(flaggedReadingsMap.sequencedKeySet());
            List<String> flaggedReadingsReasons = new ArrayList<>(flaggedReadingsMap.sequencedValues());

            List<Reading> flaggedReadings = flaggedReadingsList.get(selectedIndex);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd hh:mma");

            double totalUsage = 0;

            if (flaggedReadings.size() > 1) {
                selectedDateRange.setText("Selected: " + flaggedReadings.getFirst().getDateTime().format(formatter) + " - " + flaggedReadings.getLast().getDateTime().format(formatter));
            } else {
                selectedDateRange.setText("Selected: " + flaggedReadings.getFirst().getDateTime().format(formatter));
            }

            for (Reading reading : flaggedReadings) {
                totalUsage += reading.getQuantity();
            }

            String formattedTotal = String.format("%.1f", totalUsage);

            selectedUtilityUsed.setText("Utility used: " + formattedTotal + " " + flaggedReadings.getFirst().getUnit());

            selectedUnusualReason.setText("Reason: " + flaggedReadingsReasons.get(selectedIndex));
        }
        else {
            selectedDateRange.setText("No unusual data found");

            selectedUtilityUsed.setText("");

            selectedUnusualReason.setText("");
        }

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
        showUnusualPatterns();
    }

    @FXML
    protected void onPreviousUnusualStatClick() {
        if (selectedIndex > 0) {
            selectedIndex--;
            showSelectedUnusualStats();
            updateUnusualFlaggedChart(selectedIndex, selectedIndex + 1);
        }
    }

    @FXML
    protected void onNextUnusualStatClick() {
        if (selectedIndex < flaggedReadingsMap.size() - 1) {
            selectedIndex++;
            showSelectedUnusualStats();
            updateUnusualFlaggedChart(selectedIndex, selectedIndex - 1);
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

        updateUnusualConsumptionData();

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
        List<XYChart<String, Number>> allCharts = List.of(consumptionChart, distributionChart);

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


    // hides all tab content and removes it from the layout
    // each show() method can then show only the needed content for that tab
    private void hideAllTabContent() {
        // the utility toggle is on by default; only Trends hides it
        utilityToggle.setVisible(true);
        utilityToggle.setManaged(true);

        trendmodeToggle.setVisible(false);
        trendmodeToggle.setManaged(false);

        comparisonmodeToggle.setVisible(false);
        comparisonmodeToggle.setManaged(false);

        overviewContentBox.setVisible(false);
        overviewContentBox.setManaged(false);

        trendsContentBox.setVisible(false);
        trendsContentBox.setManaged(false);

        comparisonsContentBox.setVisible(false);
        comparisonsContentBox.setManaged(false);

        unusualPatternsContentBox.setVisible(false);
        unusualPatternsContentBox.setManaged(false);

        statisticsContentBox.setVisible(false);
        statisticsContentBox.setManaged(false);

        comingSoonLabel.setVisible(false);
        comingSoonLabel.setManaged(false);
    }
}