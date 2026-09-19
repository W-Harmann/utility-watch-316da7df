package com.utilitywatch.reports;

import com.utilitywatch.Reading;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

// shared helper for filtering a user's readings down to a resource type and date range,
// used by both the Reports screen and the Customise Report screen
public class ReadingFilter {

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