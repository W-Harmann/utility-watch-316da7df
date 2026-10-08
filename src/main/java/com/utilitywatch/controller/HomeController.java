package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import java.util.List;

public class HomeController {

    @FXML
    private Label welcomeLabel;
    @FXML
    private LineChart<String, Number> trendChart;

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
        loadExampleTrendData();
    }

    // currently loads the trend chart with example data
    private void loadExampleTrendData() {
        List<String> days = List.of("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun");
        List<Double> energyValues = List.of(10.2, 11.5, 9.8, 12.4, 13.1, 8.6, 10.9);
        List<Double> waterValues = List.of(150.5, 175.0, 167.0, 180.1, 155.0, 190.0, 170.0);

        XYChart.Series<String, Number> energySeries = new XYChart.Series<>();
        energySeries.setName("Energy (kWh)");
        for (int day = 0; day < days.size(); day++) {
            energySeries.getData().add(new XYChart.Data<>(days.get(day), energyValues.get(day)));
        }

        XYChart.Series<String, Number> waterSeries = new XYChart.Series<>();
        waterSeries.setName("Water (L)");
        for (int day = 0; day < days.size(); day++) {
            waterSeries.getData().add(new XYChart.Data<>(days.get(day), waterValues.get(day)));
        }

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

    // TODO: link this after conservation screens are done
    @FXML
    protected void onLearnMoreClick() {
        // button currently does nothing
    }
}