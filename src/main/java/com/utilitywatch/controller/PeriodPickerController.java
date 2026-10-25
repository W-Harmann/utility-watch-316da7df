package com.utilitywatch.controller;

import com.utilitywatch.login.Session;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;

/**
 * Controller for period picker ({@code period-picker.fxml}). Allows the user to get readings inbetween two dates.
 * Updates on a date changing.
 */
public class PeriodPickerController {

    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;

    private Runnable onPeriodChanged;

    /**
     * Fills both date pickers with the shared date range (defaulting to the user's first to latest
     * reading) and starts listening for changes. Called by the FXML loader.
     */
    @FXML
    public void initialize() {
        // period is shared between pages; by default it runs from the first to the latest reading
        int userId = Session.getInstance().getCurrentUser().getId();
        SharedDateRange.useDefaultIfNotChosen(SharedReadingDAO.getDAO().getReadingsForUser(userId));
        fromDatePicker.setValue(SharedDateRange.getFrom());
        toDatePicker.setValue(SharedDateRange.getTo());

        fromDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> handlePeriodChange());
        toDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> handlePeriodChange());
    }

    // called whenever either date picker changes and swaps the dates if the user
    // picked them the wrong way round, then notifies the parent controller
    private void handlePeriodChange() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to = toDatePicker.getValue();

        if (from == null || to == null) {
            return;
        }

        // setValue re-triggers this listener which will notify onPeriodChanged
        if (from.isAfter(to)) {
            fromDatePicker.setValue(to);
            toDatePicker.setValue(from);
            return;
        }

        // remember the chosen period for the other pages
        SharedDateRange.set(from, to);

        if (onPeriodChanged != null) {
            onPeriodChanged.run();
        }
    }

    /**
     * Sets the action to run whenever the user picks a new, valid period.
     *
     * @param onPeriodChanged the action to run, usually the parent controller's refresh
     */
    public void setOnPeriodChanged(Runnable onPeriodChanged) {
        this.onPeriodChanged = onPeriodChanged;
    }

    /**
     * Returns the first day of the selected period.
     *
     * @return the From date, or {@code null} if none is selected
     */
    public LocalDate getStart() {
        return fromDatePicker.getValue();
    }

    /**
     * Returns the last day of the selected period.
     *
     * @return the To date, or {@code null} if none is selected
     */
    public LocalDate getEnd() {
        return toDatePicker.getValue();
    }

    /**
     * Sets the start and end dates in the date pickers, for example when the report period is passed
     * from another controller. The dates are set in an order that never leaves the pickers backwards in
     * between, otherwise the automatic swap would kick in (e.g. moving from a January range to an
     * October one).
     *
     * @param start the first day of the period
     * @param end the last day of the period
     */
    public void setPeriod(LocalDate start, LocalDate end) {
        LocalDate currentEnd = toDatePicker.getValue();
        if (currentEnd != null && start != null && start.isAfter(currentEnd)) {
            toDatePicker.setValue(end);
            fromDatePicker.setValue(start);
        } else {
            fromDatePicker.setValue(start);
            toDatePicker.setValue(end);
        }
    }
}
