package com.utilitywatch.controller;

import com.utilitywatch.home.DashboardSummary;
import com.utilitywatch.home.DashboardSummaryService;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

/** shows the user's latest readings, goals, and alerts on the home dashboard */
public class HomeController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label summaryLabel;
    @FXML
    private Label energyValueLabel;
    @FXML
    private Label waterValueLabel;
    @FXML
    private Label costValueLabel;
    @FXML
    private LineChart<String, Number> trendChart;

    private final DashboardSummaryService summaryService = new DashboardSummaryService();

    // references MainController so buttons like the cost tile can navigate to other screens
    private MainController mainController;

    // called when home loads so it can use MainController to navigate to other screens such as costs
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        // TODO: add username to register/login for a personalised greeting (if user entered one)
        welcomeLabel.setText("Welcome back!");
        loadReadingData();
    }

    // fills the tiles, cost estimate, and 7 day chart from the user's readings
    private void loadReadingData() {
        Optional<DashboardSummary> summary = summaryService.buildSummary();

        // shows a message and clears the values if there are no readings to display
        if (summary.isEmpty()) {
            summaryLabel.setText("No readings yet. Add readings to see your summary.");
            energyValueLabel.setText("No data");
            waterValueLabel.setText("No data");
            costValueLabel.setText("No data");
            return;
        }

        DashboardSummary data = summary.get();

        // shows the date of the latest readings used in the summary and the total energy and water used on that day
        summaryLabel.setText("Showing your latest readings from " + data.day().format(DateTimeFormatter.ofPattern("EEE d MMM yyyy")) + ".");
        energyValueLabel.setText(String.format("%.1f kWh", data.energyTotal()));
        waterValueLabel.setText(String.format("%.0f Litres", data.waterTotal()));

        // calculates and displays the daily cost using teh rates saved on the costs screen
        double cost = summaryService.estimateDailyCost(data.day(), SharedUtilitySettings.getEnergyRate(), SharedUtilitySettings.getWaterRate());
        costValueLabel.setText(String.format("$%.2f AUD", cost));

        loadTrendChart(data);
    }

    // builds the energy and water chart using the last 7 days of readings
    private void loadTrendChart(DashboardSummary data) {
        DateTimeFormatter dayFormat = DateTimeFormatter.ofPattern("EEE d");

        XYChart.Series<String, Number> energySeries = new XYChart.Series<>();
        energySeries.setName("Energy (kWh)");
        XYChart.Series<String, Number> waterSeries = new XYChart.Series<>();
        waterSeries.setName("Water (L)");

        // adds each day's date and usage total to the matching resource series
        for (int day = 0; day < data.trendDays().size(); day++) {
            String dayLabel = data.trendDays().get(day).format(dayFormat);
            energySeries.getData().add(new XYChart.Data<>(dayLabel, data.energyTrend().get(day)));
            waterSeries.getData().add(new XYChart.Data<>(dayLabel, data.waterTrend().get(day)));
        }

        // removes the old chart data before adding the updated readings
        trendChart.getData().clear();
        trendChart.getData().addAll(energySeries, waterSeries);
    }

    // sends the user to the reports screen
    @FXML
    protected void onEnergyTileClick() {
        if (mainController != null) {
            mainController.showReportsOverview();
        }
    }

    // sends the user to the reports screen
    @FXML
    protected void onWaterTileClick() {
        if (mainController != null) {
            mainController.showReportsOverview();
        }
    }

    // sends the user to the costs screen
    @FXML
    protected void onCostTileClick() {
        if (mainController != null) {
            mainController.showCosts();
        }
    }

    // sends the user to the goals screen when they click anywhere in the goal progress box
    @FXML
    protected void onGoalProgressClick() {
        if (mainController != null) {
            mainController.showGoals();
        }
    }

    // sends the user to the alerts screen
    @FXML
    protected void onViewAllAlertsClick() {
        if (mainController != null) {
            mainController.showAlerts();
        }
    }

    // opens customise report screen for data export
    @FXML
    protected void onExportDataClick() {
        if (mainController != null) {
            mainController.showCustomiseReport();
        }
    }

    // TODO: link this after conservation screens are done
    @FXML
    protected void onLearnMoreClick() {
        if (mainController != null) {
            mainController.showConservation();
        }
    }
}