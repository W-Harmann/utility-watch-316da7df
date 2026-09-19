package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.Month;
import java.time.Period;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import javafx.scene.chart.BarChart;
import javafx.application.Platform;

public class ReportsController {
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

    // reference to MainController for screen navigation
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        loadMockChartData();
        loadMockDistributionData();

        // updates the chart style whenever the utility toggle changes
        utilityToggleGroup.selectedToggleProperty().addListener(
                (obs, oldToggle, newToggle) -> updateDistributionChartColour());
        // applies the correct chart style when the screen first loads
        updateDistributionChartColour();
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

        //todo: Database integration
        //fixme: Set year by years collected in DataBase readings
        trendYearDropdown.getItems().setAll("2026","2025","2023","2022","2021","2020");

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

        //fixme: Insert values from Reading on-to graph
        // current values are placeholders
        List<Double> EnergyReadingsByPeriod = IntStream.rangeClosed(1, x_axis.size()).boxed().map(Double::valueOf).toList();
        List<Double> WaterReadingsByPeriod = IntStream.rangeClosed(1, x_axis.size()).boxed().map(Double::valueOf).toList();

        // creates the energy consumption data series and adds each energy value to its date
        XYChart.Series<String, Number> EnergyGraph = new XYChart.Series<>();
        EnergyGraph.setName("Energy Trend " + YearSelected);
        for (int idx = 0; idx < x_axis.size(); idx++) {
            EnergyGraph.getData().add(new XYChart.Data<>(x_axis.get(idx), EnergyReadingsByPeriod.get(idx)));
        }

        // creates the water consumption data series and adds each water value to its date
        XYChart.Series<String, Number> WaterGraph = new XYChart.Series<>();
        WaterGraph.setName("Water Trend " + YearSelected);
        for (int idx = 0; idx < x_axis.size(); idx++) {
            WaterGraph.getData().add(new XYChart.Data<>(x_axis.get(idx), WaterReadingsByPeriod.get(idx)));
        }

        // clean graph
        trendEnergyGraph.getData().clear();
        trendWaterGraph.getData().clear();

        // adds data series to the consumption chart
        trendEnergyGraph.getData().addAll(EnergyGraph);
        trendWaterGraph.getData().addAll(WaterGraph);

        //todo: add data for AVERAGE line
        //todo: add datapoints for Graph
        //todo: show legends/keys?

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
        energyToggle.setSelected(true);
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
    private void updateDistributionChartColour() {
        // removes any previously applied utility toggle
        distributionChart.getStyleClass().removeAll("energy-selected", "water-selected", "both-selected");

        // applies the specific chart style related to the selected utility type
        if (energyToggle.isSelected()) {
            distributionChart.getStyleClass().add("energy-selected");
        } else if (waterToggle.isSelected()) {
            distributionChart.getStyleClass().add("water-selected");
        } else if (bothToggle.isSelected()) {
            distributionChart.getStyleClass().add("both-selected");
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