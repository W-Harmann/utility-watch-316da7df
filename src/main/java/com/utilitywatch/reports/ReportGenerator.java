package com.utilitywatch.reports;

import java.util.List;
import java.util.Optional;

public class ReportGenerator {

    private final ConsumptionSummaryCalculator calculator = new ConsumptionSummaryCalculator();

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

    public boolean hasSufficientData(List<Double> readings) {
        return !readings.isEmpty();
    }
}