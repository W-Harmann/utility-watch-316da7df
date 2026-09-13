package com.utilitywatch.reports;

// US4.2 - As a power plant manager, I want to know seasonal patterns present in the power consumption of the plant with an optional numeric form so that I can adjust to decrease or increase the required quotas by the city.

// US4.3 - As an environmental activist, I want to know my own monthly electrical and hydro usage trends in detail, so that I can minimalize my lifestyle.

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Month;
import java.util.List;
import java.util.Optional;

class TrendCalculatorTest {

    private TrendCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new TrendCalculator();
    }

    @Nested
    class SharedUsageValues {

        // AC (US4.2): "...a line chart with the total and average usage as the values over MONTHS."
        // AC (US4.3): "Both include the total and average usage as the values over DAYS."
        @Test
        void shouldCalculateTotalUsage() {
            List<Double> readings = List.of(2.5, 3.5, 4.0);

            double total = calculator.calculateTotal(readings);

            assertEquals(10.0, total, 0.01);
        }

        // AC (US4.2 / US4.3): "...the total and average usage as the values..."
        @Test
        void shouldCalculateAverageUsage() {
            List<Double> readings = List.of(2.5, 3.5, 4.0);

            double average = calculator.calculateAverage(readings);

            assertEquals(3.33, average, 0.01);
        }

        // AC (US4.2 / US4.3): a bucket inside the selected range with no readings is plotted as zero so the series stays continuous.
        @Test
        void shouldTreatBucketWithNoReadingsAsZeroTotal() {
            assertEquals(0.0, calculator.calculateTotal(List.of()), 0.01);
        }

        // AC (US4.3): "The application will display 2 line charts. One for POWER and One for WATER." Power and water are measured in different units, so their series are calculated independently and never combined.
        @Test
        void shouldCalculatePowerAndWaterSeriesIndependently() {
            List<Double> powerReadings = List.of(4.0, 6.0);
            List<Double> waterReadings = List.of(90.0, 110.0);

            assertEquals(10.0, calculator.calculateTotal(powerReadings), 0.01);
            assertEquals(200.0, calculator.calculateTotal(waterReadings), 0.01);
        }

        // AC (US4.2 / US4.3): "...will display the total / (%) change in usage from the previous date."
        @Test
        void shouldCalculateAbsoluteChangeFromPreviousPoint() {
            double change = calculator.calculateChange(12.0, 15.0);

            assertEquals(3.0, change, 0.01);
        }

        // AC (US4.2 / US4.3): "...the total / (%) change in usage from the previous date."
        @Test
        void shouldCalculatePercentageIncreaseFromPreviousPoint() {
            Optional<Double> change = calculator.calculatePercentageChange(Optional.of(12.0), 15.0);

            assertTrue(change.isPresent());
            assertEquals(25.0, change.get(), 0.01);
        }

        // AC (US4.2 / US4.3): "...the total / (%) change in usage from the previous date."
        @Test
        void shouldCalculatePercentageDecreaseFromPreviousPoint() {
            Optional<Double> change = calculator.calculatePercentageChange(Optional.of(20.0), 15.0);

            assertTrue(change.isPresent());
            assertEquals(-25.0, change.get(), 0.01);
        }

        // AC (US4.2): first month of the quarter. AC (US4.3): first day of the month. Neither has a previous point inside the selected range, so no percentage is produced.
        @Test
        void shouldReturnEmptyPercentageChangeForFirstPointInRange() {
            Optional<Double> change = calculator.calculatePercentageChange(Optional.empty(), 15.0);

            assertTrue(change.isEmpty());
        }

        // AC (US4.2 / US4.3): a previous total of zero would divide by zero, so no percentage is produced even though the data is present.
        @Test
        void shouldReturnEmptyPercentageChangeWhenPreviousTotalIsZero() {
            Optional<Double> change = calculator.calculatePercentageChange(Optional.of(0.0), 15.0);

            assertTrue(change.isEmpty());
        }
    }

    @Nested
    class QuarterlyTimeframe {

        // AC (US4.2): "The user can select QUARTERLY as a timeframe."Q1 is the lower boundary of the quarter-to-month mapping.
        @Test
        void shouldMapFirstQuarterToJanuaryThroughMarch() {
            List<Month> months = calculator.monthsInQuarter(1);

            assertEquals(List.of(Month.JANUARY, Month.FEBRUARY, Month.MARCH), months);
        }

        // AC (US4.2): "The user can select QUARTERLY as a timeframe." Q4 is the upper boundary of the quarter-to-month mapping.
        @Test
        void shouldMapFourthQuarterToOctoberThroughDecember() {
            List<Month> months = calculator.monthsInQuarter(4);

            assertEquals(List.of(Month.OCTOBER, Month.NOVEMBER, Month.DECEMBER), months);
        }

        // AC (US4.2): "The user can select QUARTERLY as a timeframe."
        @Test
        void shouldMapEveryQuarterToExactlyThreeMonths() {
            assertEquals(3, calculator.monthsInQuarter(1).size());
            assertEquals(3, calculator.monthsInQuarter(2).size());
            assertEquals(3, calculator.monthsInQuarter(3).size());
            assertEquals(3, calculator.monthsInQuarter(4).size());
        }

        // AC (US4.2): "The user must select a specific YEAR and QUARTER for reference to the timeframe."
        @Test
        void shouldRejectQuarterOutsideValidRange() {
            assertFalse(calculator.isValidQuarter(0));
            assertFalse(calculator.isValidQuarter(5));
        }

        // AC (US4.2): "The user must select a specific YEAR and QUARTER for reference to the timeframe."
        @Test
        void shouldAcceptQuartersOneThroughFour() {
            assertTrue(calculator.isValidQuarter(1));
            assertTrue(calculator.isValidQuarter(4));
        }
    }

    @Nested
    class MonthlyTimeframe {

        // AC (US4.3): "The user must select a specific YEAR and MONTH for reference to the timeframe."
        @Test
        void shouldRejectMonthOutsideValidRange() {
            assertFalse(calculator.isValidMonth(0));
            assertFalse(calculator.isValidMonth(13));
        }

        // AC (US4.3): "The user must select a specific YEAR and MONTH for reference to the timeframe."
        @Test
        void shouldAcceptMonthsOneThroughTwelve() {
            assertTrue(calculator.isValidMonth(1));
            assertTrue(calculator.isValidMonth(12));
        }

        // AC (US4.3): "...the values over DAYS." The number of points on the chart is driven by the length of the month.
        @Test
        void shouldReturnThirtyOneDaysForJanuary() {
            assertEquals(31, calculator.daysInMonth(2024, 1));
        }

        // AC (US4.3): "...the values over DAYS."
        @Test
        void shouldReturnThirtyDaysForApril() {
            assertEquals(30, calculator.daysInMonth(2024, 4));
        }

        // AC (US4.3): "...the values over DAYS." February in a leap year carries an extra point on the chart.
        @Test
        void shouldReturnTwentyNineDaysForFebruaryInLeapYear() {
            assertEquals(29, calculator.daysInMonth(2024, 2));
        }

        // AC (US4.3): "...the values over DAYS."
        @Test
        void shouldReturnTwentyEightDaysForFebruaryInNonLeapYear() {
            assertEquals(28, calculator.daysInMonth(2023, 2));
        }

        // AC (US4.3): "...the values over DAYS." A century year not divisible by 400 is not a leap year.
        @Test
        void shouldReturnTwentyEightDaysForFebruaryInCenturyNonLeapYear() {
            assertEquals(28, calculator.daysInMonth(1900, 2));
        }
    }
}