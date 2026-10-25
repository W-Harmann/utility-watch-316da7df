package com.utilitywatch.reports;

import com.utilitywatch.Reading;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Shared helper for filtering a user's readings down to a resource type and date range. It is used
 * by both the Reports screen and the Customise Report screen.
 */
public class ReadingFilter {

    /**
     * Keeps the readings of one resource type that fall within a date range. Both dates are inclusive,
     * so every reading on the end day is kept.
     *
     * @param readings the readings to filter
     * @param resourceType {@code "energy"} or {@code "water"}
     * @param start the first day of the range
     * @param end the last day of the range
     * @return the matching readings, in their original order
     * @throws NullPointerException if {@code resourceType}, {@code start} or {@code end} is null, or a
     *                              reading of that type has no date
     */
    public static List<Reading> byTypeAndPeriod(List<Reading> readings, String resourceType,
                                                LocalDate start, LocalDate end) {
        LocalDateTime startDateTime = start.atStartOfDay();
        LocalDateTime endDateTime = end.plusDays(1).atStartOfDay(); // end date is inclusive

        return readings.stream()
                .filter(reading -> resourceType.equals(reading.getResourceType()))
                .filter(reading -> !reading.getDateTime().isBefore(startDateTime)
                        && reading.getDateTime().isBefore(endDateTime))
                .toList();
    }
}