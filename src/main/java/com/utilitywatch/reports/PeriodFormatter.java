package com.utilitywatch.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/** Formats a start and end date into the label text shown on period buttons. */
public class PeriodFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    /**
     * Formats a date range as text, for example {@code "Jan 5, 2026 - Jan 20, 2026"}.
     *
     * @param start the first day of the period
     * @param end the last day of the period
     * @return the label text
     */
    public static String format(LocalDate start, LocalDate end) {
        return start.format(FORMATTER) + " - " + end.format(FORMATTER);
    }
}
