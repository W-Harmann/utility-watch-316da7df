package com.utilitywatch.reports;

import java.util.List;
import java.util.Optional;

/**
 * Compares consumption between two periods and checks that there is enough data to report on.
 */
public class ReportGenerator {

    private final ConsumptionSummaryCalculator calculator = new ConsumptionSummaryCalculator();

    /**
     * Works out how the average reading changed from the previous period to the current one.
     * <p>If the previous average is 0 the result is infinite or NaN, so callers should check
     * {@link Double#isFinite(double)} before showing it.
     *
     * @param previousPeriod the previous period's reading quantities, or empty if there is no data for it
     * @param currentPeriod the current period's reading quantities
     * @return the percentage change in the average (positive means use went up), or empty if there
     *         is no previous period
     */
    public Optional<Double> compareToPreviousPeriod(Optional<List<Double>> previousPeriod, List<Double> currentPeriod) {
        // no comparison possible without previous period data
        if (previousPeriod.isEmpty()) {
            return Optional.empty();
        }

        double previousAverage = calculator.calculateAverage(previousPeriod.get());
        double currentAverage = calculator.calculateAverage(currentPeriod);

        return Optional.of(calculatePercentChange(previousAverage, currentAverage));
    }

    // % change from previous period's average to current period's average
    private double calculatePercentChange(double previousAverage, double currentAverage) {
        return ((currentAverage - previousAverage) / previousAverage) * 100;
    }

    /**
     * Says whether there are any readings to report on.
     *
     * @param readings the reading quantities
     * @return {@code true} if the list is not empty
     */
    public boolean hasSufficientData(List<Double> readings) {
        return !readings.isEmpty();
    }
}