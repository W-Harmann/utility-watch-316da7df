package com.utilitywatch.reports;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

// formats a start/end date pair into the label text shown on period buttons
public class PeriodFormatter {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d, yyyy");

    public static String format(LocalDate start, LocalDate end) {
        return start.format(FORMATTER) + " - " + end.format(FORMATTER);
    }
}
