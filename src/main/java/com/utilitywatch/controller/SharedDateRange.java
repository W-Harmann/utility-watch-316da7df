package com.utilitywatch.controller;

import com.utilitywatch.Reading;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

// The From/To date range shared by every page that has date pickers (Reports, Costs, Impact),
// so the range the user picked is still there when they move between pages.
// Held in memory only: it resets when the app restarts, and is cleared on log out.
public final class SharedDateRange {

    // used when the user has no readings yet
    private static final int NO_READINGS_DEFAULT_DAYS = 30;

    private static LocalDate from = null;
    private static LocalDate to = null;
    private static boolean chosenByUser = false;

    private SharedDateRange() {
    }

    // Until the user picks their own range, the default is the date of their first reading
    // up to the date of their latest reading. It is worked out again each time a page opens,
    // so newly added or imported readings are included. With no readings it is the last 30 days.
    public static void useDefaultIfNotChosen(List<Reading> readings) {
        if (chosenByUser && from != null && to != null) {
            return;
        }

        LocalDate first = null;
        LocalDate latest = null;
        for (Reading reading : readings) {
            if (reading.getDateTime() == null) {
                continue;
            }
            LocalDate date = reading.getDateTime().toLocalDate();
            if (first == null || date.isBefore(first)) {
                first = date;
            }
            if (latest == null || date.isAfter(latest)) {
                latest = date;
            }
        }

        if (first == null) {
            to = LocalDate.now();
            from = to.minusDays(NO_READINGS_DEFAULT_DAYS);
        } else {
            from = first;
            to = latest;
        }
    }

    // Called when the user changes a date picker. Incomplete or backwards ranges are ignored
    // (the pages already show their own error for those), so a bad range is never carried to other pages.
    public static void set(LocalDate newFrom, LocalDate newTo) {
        if (newFrom == null || newTo == null || newFrom.isAfter(newTo)) {
            return;
        }
        from = newFrom;
        to = newTo;
        chosenByUser = true;
    }

    public static LocalDate getFrom() { return from; }
    public static LocalDate getTo() { return to; }

    // forget everything, e.g. on log out so the next user starts from their own readings
    public static void clear() {
        from = null;
        to = null;
        chosenByUser = false;
    }
}
