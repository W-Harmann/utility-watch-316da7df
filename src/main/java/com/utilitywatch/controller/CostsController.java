package com.utilitywatch.controller;

import com.utilitywatch.CostBudgetEvaluator;
import com.utilitywatch.MonthlyBudgetTracker;
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
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static com.utilitywatch.ReadingEvaluator.BOTH;
import static com.utilitywatch.ReadingEvaluator.ENERGY;
import static com.utilitywatch.ReadingEvaluator.WATER;

// US9.1 (costs and budget) and US9.3 (budget warning) - the Costs page.
// Exporting (US11.1) is now done from the home screen's Export Data tile (US7.3 / US11.2).
/**
 * Controller for the Costs screen ({@code costs-view.fxml}). Shows the user's estimated costs and a monthly budget.
 * The user can change the current rates to change the estimated costs and set a budget.
 */
public class CostsController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMM d, yyyy");
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("MMMM yyyy");
    private static final DateTimeFormatter MONTH_END_FORMAT = DateTimeFormatter.ofPattern("MMM d");

    // Utility and period
    @FXML private ToggleGroup utilityGroup;
    @FXML private ToggleButton bothToggle;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;
    @FXML private Label errorLabel;

    // Estimated cost card
    @FXML private Label totalCostLabel;
    @FXML private Label periodSummaryLabel;

    // By utility card
    @FXML private VBox energyRow;
    @FXML private VBox waterRow;
    @FXML private Label energyCostLabel;
    @FXML private Label waterCostLabel;
    @FXML private ProgressBar energyBar;
    @FXML private ProgressBar waterBar;

    // Monthly budget card
    @FXML private Label budgetSubtitleLabel;
    @FXML private Label budgetSpentLabel;
    @FXML private Label budgetSpentValue;
    @FXML private Label budgetLimitValue;
    @FXML private Label budgetUsedValue;
    @FXML private Label budgetRemainingCaption;
    @FXML private Label budgetRemainingValue;
    @FXML private ProgressBar budgetBar;
    @FXML private Label budgetStatusLabel;
    @FXML private Label budgetProjectionLabel;
    @FXML private TextField budgetField;

    // Current rates bar
    @FXML private Label energyRateLabel;
    @FXML private Label waterRateLabel;
    @FXML private Button editRatesButton;
    @FXML private HBox ratesEditBox;
    @FXML private TextField energyRateField;
    @FXML private TextField waterRateField;

    private final ReadingDAO readingDAO = SharedReadingDAO.getDAO();
    private List<Reading> readings = List.of();

    /**
     * Loads the logged-in user's readings, selects both utilities, fills the date pickers with the
     * shared date range and the rate and budget fields with the saved settings, then shows the
     * estimated costs. Called by the FXML loader.
     */
    @FXML
    public void initialize() {
        int userId = Session.getInstance().getCurrentUser().getId();
        readings = readingDAO.getReadingsForUser(userId);

        utilityGroup.selectToggle(bothToggle);
        // like the design's segmented control, one option is always selected
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

        if (SharedUtilitySettings.getMonthlyBudget() > 0) {
            budgetField.setText(plain(SharedUtilitySettings.getMonthlyBudget()));
        }
        showRatesEditor(false);
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
        showRates();
        refreshPeriodCards();
        refreshBudgetCard();
    }

    private void refreshPeriodCards() {
        String selection = selectedUtility();
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();
        if (from == null || to == null) {
            showError("Choose both a start and end date");
            clearPeriodCards();
            return;
        }
        CostBudgetEvaluator evaluator = configure(new CostBudgetEvaluator(), from.atStartOfDay(), to.atTime(LocalTime.MAX));
        if (!evaluator.getTimeRangeValidity()) {
            showError("The end date must be on or after the start date");
            clearPeriodCards();
            return;
        }

        totalCostLabel.setText(evaluator.formatUtilityCost(selection));
        periodSummaryLabel.setText(DATE_FORMAT.format(from) + " – " + DATE_FORMAT.format(to));

        BigDecimal total = evaluator.calculateUtilityCostExact(selection);
        BigDecimal energy = evaluator.calculateUtilityCostExact(ENERGY);
        BigDecimal water = evaluator.calculateUtilityCostExact(WATER);

        setShown(energyRow, includes(selection, ENERGY));
        setShown(waterRow, includes(selection, WATER));
        energyCostLabel.setText("$" + energy.toPlainString());
        waterCostLabel.setText("$" + water.toPlainString());
        energyBar.setProgress(share(energy, total));
        waterBar.setProgress(share(water, total));
    }

    // US9.3 - the budget card always tracks the current calendar month (both utilities), whatever period is
    // selected above. It shows spent so far, budget, % used and remaining, and warns when the month-end
    // projection is heading over budget (or spending already is), so the user can adjust in time.
    private void refreshBudgetCard() {
        MonthlyBudgetTracker tracker = new MonthlyBudgetTracker(readings,
                SharedUtilitySettings.getEnergyRate(), SharedUtilitySettings.getWaterRate(),
                SharedUtilitySettings.getMonthlyBudget(), LocalDate.now());
        YearMonth month = tracker.getMonth();
        String spent = "$" + tracker.getSpentSoFar().toPlainString();
        String monthEnd = MONTH_END_FORMAT.format(month.atEndOfMonth());

        setBudgetState(null);
        budgetSpentValue.setText(spent);
        budgetRemainingCaption.setText("REMAINING");

        if (!tracker.hasBudget()) {
            // no budget: no warning, just what has been spent
            budgetSubtitleLabel.setText("No budget set • " + MONTH_FORMAT.format(month));
            budgetSpentLabel.setText(spent + " spent this month");
            budgetLimitValue.setText("–");
            budgetUsedValue.setText("–");
            budgetRemainingValue.setText("–");
            budgetBar.setProgress(0);
            budgetStatusLabel.setText("Enter a monthly budget below to track your spending");
            budgetProjectionLabel.setText("");
            return;
        }

        String budget = "$" + tracker.getBudget().toPlainString();
        budgetSubtitleLabel.setText("Limit: " + budget + " • " + MONTH_FORMAT.format(month)
                + " • day " + tracker.getDaysElapsed() + " of " + tracker.getDaysInMonth());
        budgetSpentLabel.setText(spent + " out of " + budget);
        budgetLimitValue.setText(budget);
        budgetUsedValue.setText(percent(tracker.getPercentUsed()));
        budgetRemainingValue.setText("$" + tracker.getRemaining().toPlainString());
        budgetBar.setProgress(Math.min(tracker.getPercentUsed() / 100, 1.0)); // the bar stops at full; the text shows the true figure

        String projected = "$" + tracker.getProjectedMonthTotal().toPlainString();
        switch (tracker.getStatus()) {
            case NO_READINGS -> {
                // budget set but nothing to project from yet: no warning
                budgetStatusLabel.setText("No readings yet this month");
                budgetProjectionLabel.setText("Add readings for " + MONTH_FORMAT.format(month)
                        + " to see how your spending is tracking against your budget.");
            }
            case OVER_BUDGET -> {
                setBudgetState("over");
                budgetRemainingCaption.setText("OVER BY");
                budgetRemainingValue.setText("$" + tracker.getAmountOver().toPlainString());
                budgetStatusLabel.setText("⚠ Over budget • you have spent " + percent(tracker.getPercentUsed())
                        + " of your budget");
                budgetProjectionLabel.setText("At this rate you'll spend about " + projected + " by " + monthEnd + ".");
            }
            case AT_RISK -> {
                setBudgetState("at-risk");
                budgetStatusLabel.setText("⚠ On track to exceed your budget • projected " + projected + " by " + monthEnd
                        + " (" + percent(tracker.getProjectedPercent()) + " of budget)");
                budgetProjectionLabel.setText(tracker.getDaysRemaining() > 0
                        ? "To stay within budget, keep spending under $" + tracker.getDailyAllowance().toPlainString()
                        + " a day for the remaining " + days(tracker.getDaysRemaining()) + "."
                        : "This is the last day of the month.");
            }
            case ON_TRACK -> {
                setBudgetState("on-track");
                budgetStatusLabel.setText("On track • projected " + projected + " by " + monthEnd
                        + " (" + percent(tracker.getProjectedPercent()) + " of budget)");
                budgetProjectionLabel.setText("Projection assumes you keep spending at your current daily rate.");
            }
            default -> {
                // NO_BUDGET is handled above
            }
        }
    }

    // colours the bar and status text for the budget state: "on-track", "at-risk", "over", or null for neutral
    private void setBudgetState(String state) {
        budgetBar.getStyleClass().removeAll("over-budget", "at-risk-budget", "on-track-budget");
        budgetStatusLabel.getStyleClass().removeAll("over-text", "at-risk-text", "on-track-text");
        budgetRemainingValue.getStyleClass().remove("over-text");
        if (state == null) {
            return;
        }
        switch (state) {
            case "over" -> {
                budgetBar.getStyleClass().add("over-budget");
                budgetStatusLabel.getStyleClass().add("over-text");
                budgetRemainingValue.getStyleClass().add("over-text");
            }
            case "at-risk" -> {
                budgetBar.getStyleClass().add("at-risk-budget");
                budgetStatusLabel.getStyleClass().add("at-risk-text");
            }
            default -> {
                budgetBar.getStyleClass().add("on-track-budget");
                budgetStatusLabel.getStyleClass().add("on-track-text");
            }
        }
    }

    private static String days(int count) {
        return count + (count == 1 ? " day" : " days");
    }

    private void showRates() {
        energyRateLabel.setText("■ Electricity: $" + plain(SharedUtilitySettings.getEnergyRate()) + "/kWh");
        waterRateLabel.setText("■ Water: $" + plain(SharedUtilitySettings.getWaterRate()) + "/L");
    }

    private void clearPeriodCards() {
        totalCostLabel.setText("–");
        periodSummaryLabel.setText("");
        energyCostLabel.setText("");
        waterCostLabel.setText("");
        energyBar.setProgress(0);
        waterBar.setProgress(0);
    }

    // ---- Button handlers ----

    @FXML
    private void onSetBudgetClick() {
        try {
            SharedUtilitySettings.setMonthlyBudget(parseAmount(budgetField, "Budget"));
            refresh();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onEditRatesClick() {
        energyRateField.setText(plain(SharedUtilitySettings.getEnergyRate()));
        waterRateField.setText(plain(SharedUtilitySettings.getWaterRate()));
        showRatesEditor(true);
    }

    @FXML
    private void onSaveRatesClick() {
        try {
            double energyRate = parseAmount(energyRateField, "Electricity rate");
            double waterRate = parseAmount(waterRateField, "Water rate");
            SharedUtilitySettings.setEnergyRate(energyRate);
            SharedUtilitySettings.setWaterRate(waterRate);
            showRatesEditor(false);
            refresh();
        } catch (IllegalArgumentException e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onCancelRatesClick() {
        showRatesEditor(false);
        errorLabel.setText("");
    }

    // ---- Helpers ----

    /** Gives an evaluator the readings, period, rates and budget. */
    private <T extends CostBudgetEvaluator> T configure(T evaluator, LocalDateTime from, LocalDateTime to) {
        evaluator.setReadings(readings);
        evaluator.setTimeRange(from, to);
        evaluator.setUtilityCost(ENERGY, SharedUtilitySettings.getEnergyRate());
        evaluator.setUtilityCost(WATER, SharedUtilitySettings.getWaterRate());
        evaluator.setMonthlyBudget(SharedUtilitySettings.getMonthlyBudget());
        return evaluator;
    }

    private String selectedUtility() {
        return (String) utilityGroup.getSelectedToggle().getUserData();
    }

    private static boolean includes(String selection, String utility) {
        return BOTH.equals(selection) || utility.equals(selection);
    }

    /** Reads a non-negative number from a text field, with a clear message if it isn't one. */
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

    private static String percent(double value) {
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP).toPlainString() + "%";
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }

    private void showRatesEditor(boolean editing) {
        setShown(ratesEditBox, editing);
        setShown(editRatesButton, !editing);
    }

    private static void setShown(Node node, boolean shown) {
        node.setVisible(shown);
        node.setManaged(shown);
    }

    private void showError(String message) {
        errorLabel.setText(message);
    }
}
