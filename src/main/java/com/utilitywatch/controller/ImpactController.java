package com.utilitywatch.controller;

import com.utilitywatch.EnvironmentalEvaluator;
import com.utilitywatch.EnvironmentalEvaluator.ChangeDirection;
import com.utilitywatch.EnvironmentalEvaluator.FootprintPoint;
import com.utilitywatch.EnvironmentalEvaluator.Grouping;
import com.utilitywatch.FootprintTrendFormatter;
import com.utilitywatch.Reading;
import com.utilitywatch.database.ReadingDAO;
import com.utilitywatch.login.Session;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static com.utilitywatch.ReadingEvaluator.BOTH;
import static com.utilitywatch.ReadingEvaluator.ENERGY;
import static com.utilitywatch.ReadingEvaluator.WATER;

// US10.1 - the Environmental Impacts page
// US10.3 - footprint over time chart (weekly or monthly)
/**
 * Controller for the Impact screen ({@code impact-view.fxml}). Shows the user's estimated environmental impact and a
 * footprint over time graph. The user can change the current emission factors to change the estimated impact.
 */
public class ImpactController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter SHORT_DATE = DateTimeFormatter.ofPattern("MMM d");

    // chart line colours, the same as the Energy and Water toggles
    private static final String ENERGY_COLOUR = "#FFBA4A";
    private static final String WATER_COLOUR = "#5FB2F5";

    // Utility and period
    @FXML private ToggleGroup utilityGroup;
    @FXML private ToggleButton bothToggle;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Label errorLabel;

    // Footprint card
    @FXML private Label footprintLabel;
    @FXML private Label changeLabel;

    // By utility card
    @FXML private VBox energyRow;
    @FXML private VBox waterRow;
    @FXML private Label energyImpactLabel;
    @FXML private Label waterImpactLabel;
    @FXML private ProgressBar energyBar;
    @FXML private ProgressBar waterBar;

    // Footprint over time card (US10.3)
    @FXML private ToggleGroup groupingGroup;
    @FXML private ToggleButton weeklyToggle;
    @FXML private ToggleButton monthlyToggle;
    @FXML private VBox trendChartBox;
    @FXML private Label trendSummaryLabel;
    @FXML private HBox trendLegend;
    private final FootprintTrendChart trendChart = new FootprintTrendChart();

    // Emission factors bar
    @FXML private Label energyFactorLabel;
    @FXML private Label waterFactorLabel;
    @FXML private Button editFactorsButton;
    @FXML private HBox factorsEditBox;
    @FXML private TextField energyFactorField;
    @FXML private TextField waterFactorField;

    private final ReadingDAO readingDAO = SharedReadingDAO.getDAO();
    private List<Reading> readings = List.of();

    @FXML
    public void initialize() {
        int userId = Session.getInstance().getCurrentUser().getId();
        readings = readingDAO.getReadingsForUser(userId);

        utilityGroup.selectToggle(bothToggle);
        utilityGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                utilityGroup.selectToggle(oldToggle);
            } else {
                refresh();
            }
        });

        // period is shared between pages; by default it runs from the first to the latest reading
        SharedDateRange.useDefaultIfNotChosen(readings);
        fromDatePicker.setValue(SharedDateRange.getFrom());
        toDatePicker.setValue(SharedDateRange.getTo());
        fromDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> onPeriodChanged());
        toDatePicker.valueProperty().addListener((obs, oldValue, newValue) -> onPeriodChanged());

        // footprint over time: starts weekly for periods up to about 3 months, monthly for longer ones; the user can switch
        trendChartBox.getChildren().add(trendChart);
        VBox.setVgrow(trendChart, Priority.ALWAYS);
        selectDefaultGrouping();
        groupingGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                groupingGroup.selectToggle(oldToggle);   // always keep one grouping selected
            } else {
                refresh();
            }
        });

        showFactorsEditor(false);
        refresh();
    }

    // ---- Refreshing the page ----

    // remember the chosen period for the other pages, then update this one
    private void onPeriodChanged() {
        SharedDateRange.set(fromDatePicker.getValue(), toDatePicker.getValue());
        refresh();
    }

    private void refresh() {
        errorLabel.setText("");
        showFactors();

        String selection = selectedUtility();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from == null || to == null) {
            showError("Choose both a start and end date");
            clearCards();
            return;
        }

        EnvironmentalEvaluator evaluator = new EnvironmentalEvaluator();
        evaluator.setReadings(readings);
        evaluator.setTimeRange(from.atStartOfDay(), to.atTime(LocalTime.MAX));
        evaluator.setEmissionRate(ENERGY, SharedUtilitySettings.getEnergyEmission());
        evaluator.setEmissionRate(WATER, SharedUtilitySettings.getWaterEmission());
        if (!evaluator.getTimeRangeValidity()) {
            showError("The end date must be on or after the start date");
            clearCards();
            return;
        }

        refreshFootprintCard(evaluator, selection, from, to);
        refreshByUtilityCard(evaluator, selection);
        refreshTrendCard(evaluator, selection);
    }

    private void refreshFootprintCard(EnvironmentalEvaluator evaluator, String selection, LocalDate from, LocalDate to) {
        footprintLabel.setText(kg(evaluator.calculateEmissionExact(selection)));

        // same dates the evaluator compares against: the same number of days immediately before
        long days = ChronoUnit.DAYS.between(from, to) + 1;
        String previousPeriod = SHORT_DATE.format(from.minusDays(days)) + " – " + DATE_FORMAT.format(to.minusDays(days));

        changeLabel.getStyleClass().removeAll("change-up", "change-down");
        if (!evaluator.hasPreviousPeriodData(selection)) {
            changeLabel.setText("No readings in the previous period (" + previousPeriod + ")");
            return;
        }

        double change = evaluator.getPercentageChange(selection);
        ChangeDirection direction = evaluator.getChangeDirection(selection);
        String amount = BigDecimal.valueOf(Math.abs(change)).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
        switch (direction) {
            case UP -> {
                changeLabel.setText("▲ " + amount + " vs previous period (" + previousPeriod + ")");
                changeLabel.getStyleClass().add("change-up");     // a higher footprint is bad
            }
            case DOWN -> {
                changeLabel.setText("▼ " + amount + " vs previous period (" + previousPeriod + ")");
                changeLabel.getStyleClass().add("change-down");   // a lower footprint is good
            }
            default -> changeLabel.setText("● No change vs previous period (" + previousPeriod + ")");
        }
    }

    private void refreshByUtilityCard(EnvironmentalEvaluator evaluator, String selection) {
        BigDecimal total = evaluator.calculateEmissionExact(selection);
        BigDecimal energy = evaluator.calculateEmissionExact(ENERGY);
        BigDecimal water = evaluator.calculateEmissionExact(WATER);

        setShown(energyRow, includes(selection, ENERGY));
        setShown(waterRow, includes(selection, WATER));
        energyImpactLabel.setText(kg(energy) + " kg");
        waterImpactLabel.setText(kg(water) + " kg");
        energyBar.setProgress(share(energy, total));
        waterBar.setProgress(share(water, total));
    }

    // US10.3 - one point per week or month across the period, following the utility selection.
    // Periods with no readings are shown as "No data" bands rather than as 0 kg.
    // With "Both" selected, energy and water are drawn as two lines; otherwise just the selected utility.
    private void refreshTrendCard(EnvironmentalEvaluator evaluator, String selection) {
        Grouping grouping = selectedGrouping();
        // footprint for the whole selection: a period with no readings at all is shown as "No data"
        List<FootprintPoint> periods = evaluator.getFootprintTrend(selection, grouping);

        List<FootprintTrendChart.Line> lines;
        if (BOTH.equals(selection)) {
            lines = List.of(
                    new FootprintTrendChart.Line("Energy", ENERGY_COLOUR, evaluator.getFootprintTrend(ENERGY, grouping)),
                    new FootprintTrendChart.Line("Water", WATER_COLOUR, evaluator.getFootprintTrend(WATER, grouping)));
        } else {
            String colour = ENERGY.equals(selection) ? ENERGY_COLOUR : WATER_COLOUR;
            lines = List.of(new FootprintTrendChart.Line(selection, colour, periods));
        }

        trendChart.show(periods, grouping, lines);
        setShown(trendLegend, BOTH.equals(selection));
        trendSummaryLabel.setText(FootprintTrendFormatter.noDataSummary(periods, grouping));
    }

    private void selectDefaultGrouping() {
        LocalDate from = SharedDateRange.getFrom();
        LocalDate to = SharedDateRange.getTo();
        boolean longPeriod = from != null && to != null && ChronoUnit.DAYS.between(from, to) > 92;
        groupingGroup.selectToggle(longPeriod ? monthlyToggle : weeklyToggle);
    }

    private void showFactors() {
        energyFactorLabel.setText("■ Electricity: " + plain(SharedUtilitySettings.getEnergyEmission()) + " kg/kWh");
        waterFactorLabel.setText("■ Water: " + plain(SharedUtilitySettings.getWaterEmission()) + " kg/L");
    }

    private void clearCards() {
        footprintLabel.setText("–");
        changeLabel.setText("");
        changeLabel.getStyleClass().removeAll("change-up", "change-down");
        energyImpactLabel.setText("");
        waterImpactLabel.setText("");
        energyBar.setProgress(0);
        waterBar.setProgress(0);
        trendChart.clearChart();
        trendSummaryLabel.setText("");
    }

    // ---- Button handlers ----

    @FXML
    private void onEditFactorsClick() {
        energyFactorField.setText(plain(SharedUtilitySettings.getEnergyEmission()));
        waterFactorField.setText(plain(SharedUtilitySettings.getWaterEmission()));
        showFactorsEditor(true);
    }

    @FXML
    private void onSaveFactorsClick() {
        try {
            double energyFactor = parseAmount(energyFactorField, "Electricity emission factor");
            double waterFactor = parseAmount(waterFactorField, "Water emission factor");
            SharedUtilitySettings.setEnergyEmission(energyFactor);
            SharedUtilitySettings.setWaterEmission(waterFactor);
            showFactorsEditor(false);
            refresh();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCancelFactorsClick() {
        showFactorsEditor(false);
        errorLabel.setText("");
    }

    // ---- Helpers ----

    private String selectedUtility() {
        return (String) utilityGroup.getSelectedToggle().getUserData();
    }

    private Grouping selectedGrouping() {
        return Grouping.valueOf((String) groupingGroup.getSelectedToggle().getUserData());
    }

    private static boolean includes(String selection, String utility) {
        return BOTH.equals(selection) || utility.equals(selection);
    }

    private static double parseAmount(TextField field, String name) {
        String text = field.getText() == null ? "" : field.getText().trim();
        if (text.isEmpty()) {
            throw new IllegalArgumentException(name + " is required");
        }
        double value;
        try {
            value = Double.parseDouble(text);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(name + " must be a number");
        }
        if (!Double.isFinite(value)) {
            throw new IllegalArgumentException(name + " must be a number");
        }
        if (value < 0) {
            throw new IllegalArgumentException(name + " cannot be negative");
        }
        return value;
    }

    private static double share(BigDecimal part, BigDecimal total) {
        return total.signum() == 0 ? 0 : part.divide(total, 4, RoundingMode.HALF_UP).doubleValue();
    }

    private static String kg(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private void showFactorsEditor(boolean editing) {
        setShown(factorsEditBox, editing);
        setShown(editFactorsButton, !editing);
    }

    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
