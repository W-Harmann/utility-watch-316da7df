package com.utilitywatch.controller;

import com.utilitywatch.Reading;
import com.utilitywatch.conservationAdvisor.ConservationAdvisor;
import com.utilitywatch.reports.ReportsDataService;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConservationController {

    @FXML
    private Label averageEnergyLabel;
    @FXML
    private Label averageWaterLabel;
    @FXML
    private Label commonPatternsEnergyLabel;
    @FXML
    private Label commonPatternsWaterLabel;
    @FXML
    private Label seasonalEnergyLabel;
    @FXML
    private Label seasonalWaterLabel;

    @FXML
    private VBox conservationMenu;
    @FXML
    private VBox energyConservationPage;
    @FXML
    private VBox waterConservationPage;
    @FXML
    private VBox commonPatternsConservationPage;
    @FXML
    private VBox seasonalConservationPage;


    // reference back to MainController to navigate to other screens
    private MainController mainController;

    private final ReportsDataService dataService = new ReportsDataService();

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {

        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        LocalDate startOfLastMonth = lastMonth.atDay(1);
        LocalDate endOfLastMonth = lastMonth.atEndOfMonth();

        LocalDate startOfCurrentMonth = YearMonth.now().atDay(1);
        LocalDate currentDay = LocalDate.now();




        List<Reading> lastMonthEnergyReadings = new ArrayList<>(dataService.getReadings("energy", startOfLastMonth, endOfLastMonth));
        List<Reading> currentMonthEnergyReadings = new ArrayList<>(dataService.getReadings("energy", startOfCurrentMonth, currentDay));

        ConservationAdvisor conservationAdvisor = new ConservationAdvisor();

        if (!lastMonthEnergyReadings.isEmpty() && !currentMonthEnergyReadings.isEmpty()) {
            double percentage = conservationAdvisor.calculatePercentChange(Optional.of(lastMonthEnergyReadings), currentMonthEnergyReadings).orElse(0.0);

            String formattedPercentage = String.format("%.2f", percentage);

            averageEnergyLabel.setText(formattedPercentage + "%");
        }
        else {
            averageEnergyLabel.setText("Not enough data");
        }

        List<Reading> lastMonthWaterReadings = new ArrayList<>(dataService.getReadings("water", startOfLastMonth, endOfLastMonth));
        List<Reading> currentMonthWaterReadings = new ArrayList<>(dataService.getReadings("water", startOfCurrentMonth, currentDay));


        if (!lastMonthWaterReadings.isEmpty() && !currentMonthWaterReadings.isEmpty()) {
            double percentage = conservationAdvisor.calculatePercentChange(Optional.of(lastMonthWaterReadings), currentMonthWaterReadings).orElse(0.0);

            String formattedPercentage = String.format("%.2f", percentage);

            averageWaterLabel.setText(formattedPercentage + "%");
        }
        else {
            averageWaterLabel.setText("Not enough data");
        }

        if (!lastMonthEnergyReadings.isEmpty() && !currentMonthEnergyReadings.isEmpty()) {

            double peakPercentage = conservationAdvisor.compareConsumptionLevel(lastMonthEnergyReadings, currentMonthEnergyReadings);

            String formattedPercentage = String.format("%.2f", peakPercentage);

            commonPatternsEnergyLabel.setText(formattedPercentage + "%");
        }
        else {
            commonPatternsEnergyLabel.setText("Not enough data");
        }

        if (!lastMonthWaterReadings.isEmpty() && !currentMonthWaterReadings.isEmpty()) {

            double peakPercentage = conservationAdvisor.compareConsumptionLevel(lastMonthWaterReadings, currentMonthWaterReadings);

            String formattedPercentage = String.format("%.2f", peakPercentage);

            commonPatternsWaterLabel.setText(formattedPercentage + "%");
        }
        else {
            commonPatternsWaterLabel.setText("Not enough data");
        }

        LocalDate firstDayOfSeason = currentDay.with(IsoFields.DAY_OF_QUARTER, 1);

        int lastMonthOfQuarter = currentDay.get(IsoFields.QUARTER_OF_YEAR) * 3;

        LocalDate lastDayOfSeason = currentDay
                .withMonth(lastMonthOfQuarter)
                .with(TemporalAdjusters.lastDayOfMonth());

        LocalDate firstDayOfLastSeason = firstDayOfSeason.minusYears(1);
        LocalDate lastDayOfLastSeason = lastDayOfSeason.minusYears(1);

        List<Reading> lastSeasonEnergyReadings = new ArrayList<>(dataService.getReadings("energy", firstDayOfLastSeason, lastDayOfLastSeason));
        List<Reading> currentSeasonEnergyReadings = new ArrayList<>(dataService.getReadings("energy", firstDayOfSeason, lastDayOfSeason));

        List<Reading> lastSeasonWaterReadings = new ArrayList<>(dataService.getReadings("water", firstDayOfLastSeason, lastDayOfLastSeason));
        List<Reading> currentSeasonWaterReadings = new ArrayList<>(dataService.getReadings("water", firstDayOfSeason, lastDayOfSeason));

        if (!lastSeasonEnergyReadings.isEmpty() && !currentSeasonEnergyReadings.isEmpty()) {
            double percentage = conservationAdvisor.calculatePercentChange(Optional.of(lastSeasonEnergyReadings), currentSeasonEnergyReadings).orElse(0.0);

            String formattedPercentage = String.format("%.2f", percentage);

            seasonalEnergyLabel.setText(formattedPercentage + "%");
        }
        else {
            seasonalEnergyLabel.setText("Not enough data");
        }

        if (!lastSeasonWaterReadings.isEmpty() && !currentSeasonWaterReadings.isEmpty()) {
            double percentage = conservationAdvisor.calculatePercentChange(Optional.of(lastSeasonWaterReadings), currentSeasonWaterReadings).orElse(0.0);

            String formattedPercentage = String.format("%.2f", percentage);

            seasonalWaterLabel.setText(formattedPercentage + "%");
        }
        else {
            seasonalWaterLabel.setText("Not enough data");
        }

    }


    @FXML
    protected void onCancelClick() {
        if (mainController != null) {
            mainController.onHomeClick();
        }
    }

    @FXML
    protected void onAverageEnergyClick() {
        hideConservations();

        energyConservationPage.setVisible(true);
        energyConservationPage.setManaged(true);
    }

    @FXML
    protected void onAverageWaterClick() {
        hideConservations();

        waterConservationPage.setVisible(true);
        waterConservationPage.setManaged(true);
    }

    @FXML
    protected void onCommonPatternsClick() {
        hideConservations();

        commonPatternsConservationPage.setVisible(true);
        commonPatternsConservationPage.setManaged(true);
    }

    @FXML
    protected void onSeasonalClick() {
        hideConservations();

        seasonalConservationPage.setVisible(true);
        seasonalConservationPage.setManaged(true);
    }

    @FXML
    protected void onCancelConservationClick() {
        hideConservations();
        conservationMenu.setVisible(true);
        conservationMenu.setManaged(true);
    }

    private void hideConservations() {
        conservationMenu.setVisible(false);
        conservationMenu.setManaged(false);

        energyConservationPage.setVisible(false);
        energyConservationPage.setManaged(false);

        waterConservationPage.setVisible(false);
        waterConservationPage.setManaged(false);

        commonPatternsConservationPage.setVisible(false);
        commonPatternsConservationPage.setManaged(false);

        seasonalConservationPage.setVisible(false);
        seasonalConservationPage.setManaged(false);
    }


}
