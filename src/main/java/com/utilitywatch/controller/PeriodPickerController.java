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

    public void setOnPeriodChanged(Runnable onPeriodChanged) {
        this.onPeriodChanged = onPeriodChanged;
    }

    public LocalDate getStart() {
        return fromDatePicker.getValue();
    }

    public LocalDate getEnd() {
        return toDatePicker.getValue();
    }

    // sets the start and end dates in the date pickers when the report period is passed from another controller
    // sets them in an order that never leaves the pickers backwards in between, otherwise the
    // auto-swap above would kick in (e.g. moving from a January range to an October one)
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
