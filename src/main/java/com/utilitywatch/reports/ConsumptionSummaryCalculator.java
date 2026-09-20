package com.utilitywatch.reports;

import java.util.Collections;
import java.util.List;

public class ConsumptionSummaryCalculator {

    private static final double LOW_VARIABILITY_THRESHOLD = 15.0;
    private static final double MODERATE_VARIABILITY_THRESHOLD = 35.0;

    // sum of all consumption readings for the period (US7.1)
    public double calculateTotal(List<Double> readings) {
        double total = 0.0;
        for (double reading : readings) {
            total += reading;
        }
        return total;
    }

    public double calculateAverage(List<Double> readings) {
        return calculateTotal(readings) / readings.size();
    }

    public double calculateMinimum(List<Double> readings) {
        return Collections.min(readings);
    }

    public double calculateMaximum(List<Double> readings) {
        return Collections.max(readings);
    }

    // middle value when readings are sorted - average of the two middle values if there is an even count
    public double calculateMedian(List<Double> readings) {
        List<Double> sorted = readings.stream().sorted().toList();
        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        } else {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        }
    }

    // how spread out the readings are from the average
    public double calculateStandardDeviation(List<Double> readings) {
        double average = calculateAverage(readings);
        double sumOfSquaredDifferences = 0.0;
        for (double reading : readings) {
            sumOfSquaredDifferences += Math.pow(reading - average, 2);
        }
        double variance = sumOfSquaredDifferences / readings.size();
        return Math.sqrt(variance);
    }

    // calculates a variability level from the coefficient of variation
    public String describeVariability(List<Double> readings) {
        // calculate the average and standard deviation needed for the coefficient of variation
        double average = calculateAverage(readings);
        double stdDev = calculateStandardDeviation(readings);

        // express the standard deviation as a percentage of the average
        double coefficientOfVariation = average == 0 ? 0 : (stdDev / average) * 100;

        // convert the percentage into a simple variability category
        if (coefficientOfVariation < LOW_VARIABILITY_THRESHOLD) {
            return "Low";
        }
        if (coefficientOfVariation < MODERATE_VARIABILITY_THRESHOLD) {
            return "Moderate";
        }
        return "High";
    }
}