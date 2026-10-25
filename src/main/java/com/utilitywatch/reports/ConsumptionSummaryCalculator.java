package com.utilitywatch.reports;

import java.util.Collections;
import java.util.List;

/**
 * Summary statistics for a list of consumption readings: total, average, median, lowest, highest,
 * standard deviation and a simple variability level (US7.1).
 * <p>The methods that need at least one reading (everything except {@link #calculateTotal(List)})
 * are not safe on an empty list, so check for readings first, for example with
 * {@link ReportGenerator#hasSufficientData(List)}.
 */
public class ConsumptionSummaryCalculator {

    private static final double LOW_VARIABILITY_THRESHOLD = 15.0;
    private static final double MODERATE_VARIABILITY_THRESHOLD = 35.0;

    /**
     * Adds up all the readings for the period (US7.1).
     *
     * @param readings the reading quantities
     * @return the sum, or 0 if the list is empty
     */
    public double calculateTotal(List<Double> readings) {
        double total = 0.0;
        for (double reading : readings) {
            total += reading;
        }
        return total;
    }

    /**
     * Works out the mean of the readings.
     *
     * @param readings the reading quantities; must not be empty
     * @return the total divided by the number of readings (NaN for an empty list)
     */
    public double calculateAverage(List<Double> readings) {
        return calculateTotal(readings) / readings.size();
    }

    /**
     * Finds the lowest reading.
     *
     * @param readings the reading quantities; must not be empty
     * @return the smallest quantity
     * @throws java.util.NoSuchElementException if the list is empty
     */
    public double calculateMinimum(List<Double> readings) {
        return Collections.min(readings);
    }

    /**
     * Finds the highest reading.
     *
     * @param readings the reading quantities; must not be empty
     * @return the largest quantity
     * @throws java.util.NoSuchElementException if the list is empty
     */
    public double calculateMaximum(List<Double> readings) {
        return Collections.max(readings);
    }

    /**
     * Finds the middle value when the readings are sorted. With an even number of readings it is the
     * average of the two middle values.
     *
     * @param readings the reading quantities; must not be empty
     * @return the median
     * @throws IndexOutOfBoundsException if the list is empty
     */
    public double calculateMedian(List<Double> readings) {
        List<Double> sorted = readings.stream().sorted().toList();
        int size = sorted.size();
        if (size % 2 == 1) {
            return sorted.get(size / 2);
        } else {
            return (sorted.get(size / 2 - 1) + sorted.get(size / 2)) / 2.0;
        }
    }

    /**
     * Measures how spread out the readings are from the average. This is the population standard
     * deviation: the sum of squared differences from the average is divided by the number of readings.
     *
     * @param readings the reading quantities; must not be empty
     * @return the standard deviation (NaN for an empty list)
     */
    public double calculateStandardDeviation(List<Double> readings) {
        double average = calculateAverage(readings);
        double sumOfSquaredDifferences = 0.0;
        for (double reading : readings) {
            sumOfSquaredDifferences += Math.pow(reading - average, 2);
        }
        double variance = sumOfSquaredDifferences / readings.size();
        return Math.sqrt(variance);
    }

    /**
     * Describes how variable the readings are, using the coefficient of variation (the standard
     * deviation as a percentage of the average). Below 15% is "Low", below 35% is "Moderate", and
     * anything higher is "High". If the average is 0 the coefficient is treated as 0, giving "Low".
     *
     * @param readings the reading quantities; must not be empty
     * @return {@code "Low"}, {@code "Moderate"} or {@code "High"}
     */
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