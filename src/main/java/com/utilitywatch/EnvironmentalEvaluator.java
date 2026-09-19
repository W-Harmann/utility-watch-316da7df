package com.utilitywatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * US10.1 - Estimates CO2-equivalent emissions (kg) from utility usage.
 */
public class EnvironmentalEvaluator extends ReadingEvaluator {

    private final Map<String, BigDecimal> emissionRates = new HashMap<>();

    /** Sets kg of CO2 per unit of usage. Negative rates are rejected. */
    public boolean setEmissionRate(String utility, double kgPerUnit) {
        requireKnownUtility(utility);
        if (kgPerUnit < 0) {
            return false;
        }
        emissionRates.put(utility, BigDecimal.valueOf(kgPerUnit));
        return true;
    }

    public double getEmissionRate(String utility) {
        requireKnownUtility(utility);
        return emissionRates.getOrDefault(utility, BigDecimal.ZERO).doubleValue();
    }

    /** Exact kg of CO2 for "energy", "water" or "both" in the time range. */
    public BigDecimal calculateEmissionExact(String selection) {
        BigDecimal total = BigDecimal.ZERO;
        for (String utility : resolveSelection(selection)) {
            BigDecimal rate = emissionRates.getOrDefault(utility, BigDecimal.ZERO);
            total = total.add(totalUsage(utility).multiply(rate));
        }
        return total;
    }

    /** Name kept to match the existing tests; "calculateEmission" would be clearer. */
    public double calculateEmissionPower(String selection) {
        return calculateEmissionExact(selection).doubleValue();
    }

    // ---- Previous-period comparison (US10.1) ----

    /** Whether the footprint went up, down or stayed the same compared with the previous period. */
    public enum ChangeDirection { UP, DOWN, NO_CHANGE }

    /**
     * Footprint for the equivalent previous period: the same number of whole days
     * immediately before the selected range (e.g. Aug 1-31 -> Jul 1-31).
     */
    public BigDecimal calculatePreviousPeriodEmissionExact(String selection) {
        List<LocalDateTime> range = requireValidRange();
        long days = ChronoUnit.DAYS.between(range.get(0).toLocalDate(), range.get(1).toLocalDate()) + 1;
        LocalDateTime previousFrom = range.get(0).minusDays(days);
        LocalDateTime previousTo = range.get(1).minusDays(days);

        BigDecimal total = BigDecimal.ZERO;
        for (String utility : resolveSelection(selection)) {
            BigDecimal rate = emissionRates.getOrDefault(utility, BigDecimal.ZERO);
            total = total.add(totalUsageBetween(utility, previousFrom, previousTo).multiply(rate));
        }
        return total;
    }

    public double calculatePreviousPeriodEmission(String selection) {
        return calculatePreviousPeriodEmissionExact(selection).doubleValue();
    }

    /** True if the previous period has a footprint to compare against. */
    public boolean hasPreviousPeriodData(String selection) {
        return calculatePreviousPeriodEmissionExact(selection).signum() > 0;
    }

    /** Signed % change from the previous period, to 2 d.p. (e.g. 20.0 or -25.0). 0 if there is no previous data. */
    public double getPercentageChange(String selection) {
        BigDecimal previous = calculatePreviousPeriodEmissionExact(selection);
        if (previous.signum() == 0) {
            return 0;
        }
        return percentageDifference(calculateEmissionExact(selection), previous);
    }

    public ChangeDirection getChangeDirection(String selection) {
        double change = getPercentageChange(selection);
        if (change > 0) {
            return ChangeDirection.UP;
        }
        if (change < 0) {
            return ChangeDirection.DOWN;
        }
        return ChangeDirection.NO_CHANGE;
    }

    // ---- State average comparison (US10.1) ----

    /** MOCK DATA: placeholder QLD household footprint (kg CO2e per day). Replace with a real figure when sourced. */
    public static final double MOCK_QLD_AVERAGE_KG_PER_DAY = 20.0;

    private BigDecimal stateAverageKgPerDay = BigDecimal.valueOf(MOCK_QLD_AVERAGE_KG_PER_DAY);

    /** @return true if stored, false if rejected (negative). */
    public boolean setStateAverageKgPerDay(double kgPerDay) {
        if (kgPerDay < 0) {
            return false;
        }
        stateAverageKgPerDay = BigDecimal.valueOf(kgPerDay);
        return true;
    }

    public double getStateAverageKgPerDay() {
        return stateAverageKgPerDay.doubleValue();
    }

    /** The State average scaled to the number of whole days in the selected range. */
    public BigDecimal getStateAverageForPeriodExact() {
        List<LocalDateTime> range = requireValidRange();
        long days = ChronoUnit.DAYS.between(range.get(0).toLocalDate(), range.get(1).toLocalDate()) + 1;
        return stateAverageKgPerDay.multiply(BigDecimal.valueOf(days));
    }

    public double getStateAverageForPeriod() {
        return getStateAverageForPeriodExact().doubleValue();
    }

    /** Signed % difference of the user's total footprint from the State average (e.g. 20.0 = 20% above). */
    public double getPercentageVsStateAverage() {
        BigDecimal average = getStateAverageForPeriodExact();
        if (average.signum() == 0) {
            return 0;
        }
        return percentageDifference(calculateEmissionExact(BOTH), average);
    }

    // ---- helpers ----

    private static double percentageDifference(BigDecimal value, BigDecimal baseline) {
        return value.subtract(baseline)
                .multiply(BigDecimal.valueOf(100))
                .divide(baseline, 2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private List<LocalDateTime> requireValidRange() {
        if (!getTimeRangeValidity()) {
            throw new IllegalStateException("A valid time range must be set first");
        }
        return getTimeRange();
    }
}
