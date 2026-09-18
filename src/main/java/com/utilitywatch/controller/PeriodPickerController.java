package com.utilitywatch.controller;

import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;
import java.time.LocalDate;

public class PeriodPickerController {

    @FXML
    private DatePicker fromDatePicker;
    @FXML
    private DatePicker toDatePicker;

    private Runnable onPeriodChanged;

    @FXML
    public void initialize() {
        // default period is last 30 days
        fromDatePicker.setValue(LocalDate.now().minusDays(30));
        toDatePicker.setValue(LocalDate.now());

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
    public void setPeriod(LocalDate start, LocalDate end) {
        fromDatePicker.setValue(start);
        toDatePicker.setValue(end);
    }
}