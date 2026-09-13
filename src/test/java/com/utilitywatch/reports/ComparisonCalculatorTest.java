package com.utilitywatch.reports;

// US3.1 - As a business overseer, I want to read a comparison of my industry's total annual water and power consumption across different periods of time so that I can make financial modifications to the business.

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDate;
import java.util.List;

class ComparisonCalculatorTest {

    private ComparisonCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new ComparisonCalculator();
    }

    // AC: "(T) Total Utility Usage of Date (Done for Both Dates)"
    @Test
    void shouldCalculateTotalForEachPeriodIndependently() {
        List<Double> periodOne = List.of(10.0, 20.0);
        List<Double> periodTwo = List.of(5.0, 5.0, 5.0);

        assertEquals(30.0, calculator.calculateTotal(periodOne), 0.01);
        assertEquals(15.0, calculator.calculateTotal(periodTwo), 0.01);
    }

    // AC: "(D) Difference in (T) Total between (Date) Date. Date 1 compared to Date 2." Sign convention: periodOne - periodTwo, so a positive result means period one consumed more than period two.
    @Test
    void shouldCalculateDifferenceBetweenPeriodTotals() {
        List<Double> periodOne = List.of(30.0, 30.0);
        List<Double> periodTwo = List.of(20.0, 20.0);

        double difference = calculator.calculateDifference(periodOne, periodTwo);

        assertEquals(20.0, difference, 0.01);
    }

    // AC: "(D) Difference in (T) Total between (Date) Date. Date 1 compared to Date 2." Locks the sign convention: a decrease from period one to period two is negative.
    @Test
    void shouldReturnNegativeDifferenceWhenSecondPeriodIsHigher() {
        List<Double> periodOne = List.of(10.0);
        List<Double> periodTwo = List.of(25.0);

        double difference = calculator.calculateDifference(periodOne, periodTwo);

        assertEquals(-15.0, difference, 0.01);
    }

    // AC: "(D) Difference in (T) Total between (Date) Date."
    @Test
    void shouldReturnZeroDifferenceWhenPeriodTotalsMatch() {
        List<Double> periodOne = List.of(10.0, 20.0);
        List<Double> periodTwo = List.of(15.0, 15.0);

        double difference = calculator.calculateDifference(periodOne, periodTwo);

        assertEquals(0.0, difference, 0.01);
    }

    // AC: "(T) Total Utility Usage of Date (Done for Both Dates)" A period with no readings is a valid selection, not an error.
    @Test
    void shouldTreatPeriodWithNoReadingsAsZeroTotal() {
        List<Double> periodOne = List.of(40.0);
        List<Double> periodTwo = List.of();

        double difference = calculator.calculateDifference(periodOne, periodTwo);

        assertEquals(40.0, difference, 0.01);
    }

    // AC: "The user can select BOTH power AND water as an option." Power and water are measured in different units, so their totals are never combined into a single figure.
    @Test
    void shouldKeepPowerAndWaterDifferencesSeparate() {
        List<Double> powerPeriodOne = List.of(100.0);
        List<Double> powerPeriodTwo = List.of(80.0);
        List<Double> waterPeriodOne = List.of(500.0);
        List<Double> waterPeriodTwo = List.of(650.0);

        double powerDifference = calculator.calculateDifference(powerPeriodOne, powerPeriodTwo);
        double waterDifference = calculator.calculateDifference(waterPeriodOne, waterPeriodTwo);

        assertEquals(20.0, powerDifference, 0.01);
        assertEquals(-150.0, waterDifference, 0.01);
    }

    // AC: "The user must select 2 different timeframe/period (Date) freely using a datetime value"
    @Test
    void shouldRejectComparisonWhenBothPeriodsAreTheSameDate() {
        LocalDate date = LocalDate.of(2024, 3, 1);

        boolean valid = calculator.isValidComparison(date, date);

        assertFalse(valid);
    }

    // AC: "The user must select 2 different timeframe/period (Date) freely using a datetime value"
    @Test
    void shouldAcceptComparisonBetweenTwoDifferentDates() {
        LocalDate periodOneStart = LocalDate.of(2023, 1, 1);
        LocalDate periodTwoStart = LocalDate.of(2024, 1, 1);

        boolean valid = calculator.isValidComparison(periodOneStart, periodTwoStart);

        assertTrue(valid);
    }

    // AC: "The user must select 2 different timeframe/period (Date) freely" "Freely" means the later date may be chosen first.
    @Test
    void shouldAcceptComparisonWhenSecondDatePrecedesFirst() {
        LocalDate periodOneStart = LocalDate.of(2024, 1, 1);
        LocalDate periodTwoStart = LocalDate.of(2023, 1, 1);

        boolean valid = calculator.isValidComparison(periodOneStart, periodTwoStart);

        assertTrue(valid);
    }

    // AC: "Application will provide text to accompany the calculations in a coherent format."
    @Test
    void shouldDescribeAnIncreaseInConsumption() {
        List<Double> periodOne = List.of(150.0);
        List<Double> periodTwo = List.of(100.0);

        String description = calculator.describeComparison(periodOne, periodTwo);

        assertTrue(description.toLowerCase().contains("increase"));
    }

    // AC: "Application will provide text to accompany the calculations in a coherent format."
    @Test
    void shouldDescribeADecreaseInConsumption() {
        List<Double> periodOne = List.of(100.0);
        List<Double> periodTwo = List.of(150.0);

        String description = calculator.describeComparison(periodOne, periodTwo);

        assertTrue(description.toLowerCase().contains("decrease"));
    }

    // AC: "Application will provide text to accompany the calculations in a coherent format."
    @Test
    void shouldDescribeNoChangeWhenTotalsMatch() {
        List<Double> periodOne = List.of(100.0);
        List<Double> periodTwo = List.of(100.0);

        String description = calculator.describeComparison(periodOne, periodTwo);

        assertTrue(description.toLowerCase().contains("no change"));
    }
}