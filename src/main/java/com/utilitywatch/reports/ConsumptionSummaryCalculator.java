package com.utilitywatch.reports;

import java.util.Collections;
import java.util.List;

public class ConsumptionSummaryCalculator {

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
}