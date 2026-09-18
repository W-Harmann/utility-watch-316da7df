package com.utilitywatch.reports;

// US7.1 - As a user, I want to view a visual summary of my energy and
// water consumption so that I can quickly understand my overall usage.

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

public class ConsumptionSummaryCalculatorTest {

    private ConsumptionSummaryCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ConsumptionSummaryCalculator();
    }

    // AC: "The summary displays the total consumption for the selected period"
    @Test
    void shouldCalculateTotalConsumption() {
        List<Double> readings = List.of(10.0, 20.0, 15.0);

        double total = calculator.calculateTotal(readings);

        assertEquals(45.0, total, 0.01);
    }

    // AC: "The summary displays relevant statistics (average, minimum and maximum consumption)"
    @Test
    void shouldCalculateAverageConsumption() {
        List<Double> readings = List.of(10.0, 20.0, 15.0);

        double average = calculator.calculateAverage(readings);

        assertEquals(15.0, average, 0.01);
    }

    // AC: "The summary displays relevant statistics (average, minimum and maximum consumption)"
    @Test
    void shouldCalculateMinimumConsumption() {
        List<Double> readings = List.of(10.0, 20.0, 5.0);

        double minimum = calculator.calculateMinimum(readings);

        assertEquals(5.0, minimum, 0.01);
    }

    // AC: "The summary displays relevant statistics (average, minimum and maximum consumption)"
    @Test
    void shouldCalculateMaximumConsumption() {
        List<Double> readings = List.of(10.0, 20.0, 5.0);

        double maximum = calculator.calculateMaximum(readings);

        assertEquals(20.0, maximum, 0.01);
    }

    // AC: "The user can select energy, water, or both, and they are clearly differentiated"
    @Test
    void shouldKeepEnergyAndWaterTotalsSeparateWhenBothSelected() {
        List<Double> energyReadings = List.of(10.0, 10.0);
        List<Double> waterReadings = List.of(150.0, 150.0);

        double energyTotal = calculator.calculateTotal(energyReadings);
        double waterTotal = calculator.calculateTotal(waterReadings);

        assertNotEquals(energyTotal, waterTotal);
    }

    // AC: "The summary updates when the user selects a different consumption type or time period"
    @Test
    void shouldReflectDifferentTotalWhenPeriodChanges() {
        List<Double> januaryReadings = List.of(10.0, 10.0);
        List<Double> februaryReadings = List.of(15.0, 15.0, 15.0);

        double januaryTotal = calculator.calculateTotal(januaryReadings);
        double februaryTotal = calculator.calculateTotal(februaryReadings);

        assertNotEquals(januaryTotal, februaryTotal);
    }
}