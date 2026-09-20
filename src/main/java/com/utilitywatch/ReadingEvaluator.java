package com.utilitywatch;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Shared behaviour for anything that works over a set of readings within a time range.
 * Both CostBudgetEvaluator and EnvironmentalEvaluator extend this, so the readings and
 * time-range logic is written (and fixed) in one place.
 */
public abstract class ReadingEvaluator {

    public static final String ENERGY = "energy";
    public static final String WATER = "water";
    public static final String BOTH = "both";
    protected static final Set<String> UTILITIES = Set.of(ENERGY, WATER);

    private List<Reading> readings = new ArrayList<>();
    private LocalDateTime fromDate;
    private LocalDateTime toDate;

    public void setReadings(List<Reading> readings) {
        this.readings = new ArrayList<>(readings); // defensive copy
    }

    public List<Reading> getReadings() {
        return List.copyOf(readings);
    }

    /** Stores the range even if it is invalid, so the UI can check validity and ask the user to fix it. */
    public void setTimeRange(LocalDateTime fromDate, LocalDateTime toDate) {
        this.fromDate = fromDate;
        this.toDate = toDate;
    }

    /** Returns [fromDate, toDate], or an empty list if no range has been set. */
    public List<LocalDateTime> getTimeRange() {
        if (fromDate == null || toDate == null) {
            return List.of();
        }
        return List.of(fromDate, toDate);
    }

    /** A range is valid when both dates are set and toDate is not before fromDate. */
    public boolean getTimeRangeValidity() {
        return fromDate != null && toDate != null && !toDate.isBefore(fromDate);
    }

    /** Sums the usage for one utility within the time range (both ends inclusive), exactly. */
    protected BigDecimal totalUsage(String utility) {
        if (fromDate != null && !getTimeRangeValidity()) {
            throw new IllegalStateException("Time range is invalid: 'to' date is before 'from' date");
        }
        BigDecimal total = BigDecimal.ZERO;
        for (Reading reading : readings) {
            if (utility.equals(reading.getResourceType()) && isInRange(reading.getDateTime())) {
                // BigDecimal.valueOf(double) uses the shortest decimal form, so 2.7 becomes exactly 2.7
                total = total.add(BigDecimal.valueOf(reading.getQuantity()));
            }
        }
        return total;
    }

    /** Sums the usage for one utility between two dates (both inclusive), ignoring the selected range. */
    protected BigDecimal totalUsageBetween(String utility, LocalDateTime from, LocalDateTime to) {
        BigDecimal total = BigDecimal.ZERO;
        for (Reading reading : readings) {
            LocalDateTime time = reading.getDateTime();
            if (utility.equals(reading.getResourceType()) && !time.isBefore(from) && !time.isAfter(to)) {
                total = total.add(BigDecimal.valueOf(reading.getQuantity()));
            }
        }
        return total;
    }

    private boolean isInRange(LocalDateTime time) {
        if (fromDate == null || toDate == null) {
            return true; // no range set: use every reading
        }
        return !time.isBefore(fromDate) && !time.isAfter(toDate);
    }

    /** Turns "energy", "water" or "both" into the list of utilities to include. */
    protected static List<String> resolveSelection(String selection) {
        if (BOTH.equals(selection)) {
            return List.of(ENERGY, WATER);
        }
        requireKnownUtility(selection);
        return List.of(selection);
    }

    protected static void requireKnownUtility(String utility) {
        if (!UTILITIES.contains(utility)) {
            throw new IllegalArgumentException("Unknown utility: " + utility);
        }
    }
}
