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

        // % change from previous period to current period
        double percentChange = ((currentAverage - previousAverage) / previousAverage) * 100;
        return Optional.of(percentChange);
    }

    public boolean hasSufficientData(List<Double> readings) {
        return !readings.isEmpty();
    }
}