package com.utilitywatch.controller;

import com.utilitywatch.Reading;

import java.time.LocalDate;
import java.util.List;

/**
 * The From/To date range shared by every page that has date pickers (Reports, Costs, Impact), so
 * the range the user picked is still there when they move between pages.
 * <p>It is held in memory only: it resets when the app restarts, and is cleared on log out.
 */
public final class SharedDateRange {

    // used when the user has no readings yet
    private static final int NO_READINGS_DEFAULT_DAYS = 30;

    private static LocalDate from = null;
    private static LocalDate to = null;
    private static boolean chosenByUser = false;

    private SharedDateRange() {
    }

    /**
     * Sets the default range, unless the user has already picked one. Until then the default is the
     * date of their first reading up to the date of their latest reading. It is worked out again each
     * time a page opens, so newly added or imported readings are included. With no readings it is the
     * last 30 days.
     *
     * @param readings the logged-in user's readings
     */
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

    /**
     * Records the range the user picked. Call it when the user changes a date picker. Incomplete or
     * backwards ranges are ignored (the pages already show their own error for those), so a bad range
     * is never carried to other pages.
     *
     * @param newFrom the first day of the range
     * @param newTo the last day of the range
     */
    public static void set(LocalDate newFrom, LocalDate newTo) {
        if (newFrom == null || newTo == null || newFrom.isAfter(newTo)) {
            return;
        }
        from = newFrom;
        to = newTo;
        chosenByUser = true;
    }

    /**
     * Returns the first day of the shared range.
     *
     * @return the From date, or {@code null} if no range has been set or defaulted yet
     */
    public static LocalDate getFrom() { return from; }
    /**
     * Returns the last day of the shared range.
     *
     * @return the To date, or {@code null} if no range has been set or defaulted yet
     */
    public static LocalDate getTo() { return to; }

    /** Forgets the range, for example on log out, so the next user starts from their own readings. */
    public static void clear() {
        from = null;
        to = null;
        chosenByUser = false;
    }
}
