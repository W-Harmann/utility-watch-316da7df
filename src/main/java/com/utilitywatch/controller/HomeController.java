package com.utilitywatch.controller;

import com.utilitywatch.goals.*;
import com.utilitywatch.home.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.OptionalDouble;

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
    private VBox goalsContainer;
    @FXML
    private VBox alertsContainer;
    @FXML
    private LineChart<String, Number> trendChart;

    private static final int MAX_ALERTS_SHOWN = 2;
    private static final int MAX_GOALS_SHOWN = 2;

    private final DashboardSummaryService summaryService = new DashboardSummaryService();
    private final GoalProgressService goalProgressService = new GoalProgressService();
    private final GoalProgressCalculator progressCalculator = new GoalProgressCalculator();
    private final GoalManager goalManager = new GoalManager();
    private final GoalRepository goalRepository = GoalRepository.getInstance();

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
        loadGoals();
        loadAlerts();
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

    // shows a progress bar for each active goal (up to the maximum) or a message if there are none
    private void loadGoals() {
        goalsContainer.getChildren().clear();
        List<Goal> activeGoals = goalManager.filterActiveGoals(goalRepository.getAll());

        if (activeGoals.isEmpty()) {
            Label emptyGoalsLabel = new Label("No active goals yet. Create one on the Goals screen.");
            emptyGoalsLabel.setWrapText(true);
            goalsContainer.getChildren().add(emptyGoalsLabel);
            return;
        }

        // only the first few goals fit on the dashboard
        int goalsToShow = Math.min(activeGoals.size(), MAX_GOALS_SHOWN);

        for (int goalIndex = 0; goalIndex < goalsToShow; goalIndex++) {
            Goal goal = activeGoals.get(goalIndex);

            // progress is null when there are no readings to compare yet
            OptionalDouble achievedReduction = goalProgressService.achievedReduction(goal);
            Double progress = achievedReduction.isPresent()
                    ? progressCalculator.progressFraction(achievedReduction.getAsDouble(), goal.getTargetReduction()) : null;
            addGoalRow(goal, progress);
        }

        // tells the user there are more goals on the goals screen
        int hiddenGoals = activeGoals.size() - goalsToShow;
        if (hiddenGoals > 0) {
            Label moreGoalsLabel = new Label("+" + hiddenGoals + " more on the Goals screen");
            moreGoalsLabel.getStyleClass().add("report-note");
            goalsContainer.getChildren().add(moreGoalsLabel);
        }
    }

    // shows the latest goal alerts (newest first) or a message if there are none
    private void loadAlerts() {
        alertsContainer.getChildren().clear();
        List<GoalAlertStore.GoalAlert> goalAlerts = GoalAlertStore.getAll();

        if (goalAlerts.isEmpty()) {
            Label emptyAlertsLabel = new Label("No recent notifications.");
            emptyAlertsLabel.setWrapText(true);
            alertsContainer.getChildren().add(emptyAlertsLabel);
            return;
        }

        int firstAlertIndex = Math.max(0, goalAlerts.size() - MAX_ALERTS_SHOWN);

        // goes from the newest alert backwards, up to the maximum allowed to be shown
        for (int alertIndex = goalAlerts.size() - 1; alertIndex >= firstAlertIndex; alertIndex--) {
            addAlertRow(goalAlerts.get(alertIndex));
        }
    }

    // loads one goal row from fxml, fills it in, and adds it to the goals container
    private void addGoalRow(Goal goal, Double progress) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/dashboard-goal-row.fxml"));
            VBox row = loader.load();

            DashboardGoalRowController controller = loader.getController();
            controller.setData(goal, progress);

            goalsContainer.getChildren().add(row);
        } catch (IOException e) {
            goalsContainer.getChildren().add(new Label("Could not load this goal"));
        }
    }

    // loads one alert row from fxml, fills it in, and adds itto the alerts container
    private void addAlertRow(GoalAlertStore.GoalAlert alert) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/dashboard-alert-row.fxml"));
            VBox row = loader.load();

            DashboardAlertRowController controller = loader.getController();
            controller.setData(alert);

            alertsContainer.getChildren().add(row);
        } catch (IOException e) {
            alertsContainer.getChildren().add(new Label("Could not load this alert"));
        }
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