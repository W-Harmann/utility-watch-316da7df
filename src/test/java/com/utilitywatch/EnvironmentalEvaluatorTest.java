package com.utilitywatch;

// US10.1 - As an environmentally conscious user I want the app to estimate the environmental impact
// so that I can understand the ecological/environmental footprint of my consumption and make more sustainable choices about utility.
//      [x] The footprint is broken down per utility type, and the per-utility figures sum to the total.
//      [x] The user can set a time range to measure impact in a range and the estimated CO₂ equivalent in kilograms for the selected period.
//      [x] The footprint is compared against the equivalent previous period, showing the percentage change and its direction.
//      [x] The users footprint is compared against the State average and displayed together.
//


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDateTime;

public class EnvironmentalEvaluatorTest {

    private EnvironmentalEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new EnvironmentalEvaluator();
    }

        //
        // Criteria: The user can set a time range to measure impact in a range and the estimated CO₂ equivalent in kilograms for the selected period.
        //

    // See *CostBudgetEvaluatorTest (for the follow test details for Readings and TimeRange)
    // -- Because to calculate emissions, the app needs to know the readings

    @Test
    void shouldStoreReadings() {

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        evaluator.setReadings(readings);

        assertEquals(
                readings,
                evaluator.getReadings()
        );
    }

    @Test
    void shouldBeStoringTimeRange() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 8, 2, 0, 0);

        evaluator.setTimeRange(fromDate, toDate);

        List<LocalDateTime> ExpectedTimeRange =
                new ArrayList<>(
                        List.of(fromDate,
                                toDate)
                );

        List<LocalDateTime> ActualTimeRange = evaluator.getTimeRange();

        assertEquals(
                ExpectedTimeRange,
                ActualTimeRange
        );
    }

    @Test
    void shouldBeReturningInvalidTimeRange() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 7, 1, 0, 0);

        evaluator.setTimeRange(fromDate, toDate);

        boolean ValidTimeRange = evaluator.getTimeRangeValidity();

        assertFalse(ValidTimeRange);
    }

    // Test Power
    @Test
    void shouldStoreEmissionByPower() {

        // store to class | assuming 1kWh produces 0.6 kg of CO2
        evaluator.setEmissionRate("energy",0.6);

        assertEquals(
                0.6,
                evaluator.getEmissionRate("energy")
        );
    }

    // Test Water
    @Test
    void shouldStoreEmissionByWater() {

        // store to class | assuming 1kLh produces 0.3 kg of CO2
        evaluator.setEmissionRate("water",0.3);

        assertEquals(
                0.3,
                evaluator.getEmissionRate("water")
        );
    }

    // Test 1
    @Test
    void shouldCalculateTimeRangeEmissionPower() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("energy", 0.6);

        // Inclusive Power Reading (3 + 5) * Co2 (0.6) = 4.8
        assertEquals(
                4.8,
                evaluator.calculateEmissionPower("energy")
        );
    }

    // Test 2
    @Test
    void shouldCalculateTimeRangeEmissionWater() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("water", 0.3);

        // Inclusive Water Reading (4 + 3) * Co2 (0.3) = 2.1
        assertEquals(
                2.1,
                evaluator.calculateEmissionPower("water")
        );
    }

    // Test 3
    @Test
    void shouldCalculateTimeRangeEmissionBoth() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("energy", 0.6);
        evaluator.setEmissionRate("water", 0.3);

        // Inclusive Power Reading (3 + 5) * Co2 (0.6) = 4.8
        // Inclusive Water Reading (4 + 3) * Co2 (0.3) = 2.1
        // Sum (4.8 + 2.1) = 6.9
        assertEquals(
                6.9,
                evaluator.calculateEmissionPower("both")
        );
    }

    //
    // Criteria: The footprint is compared against the equivalent previous period,
    //           showing the percentage change and its direction.
    //
    // The previous period is the same number of whole days immediately before the selected range,
    // e.g. Aug 1 - Aug 31 (31 days) is compared with Jul 1 - Jul 31.

    // Current period (August): energy 10 + 14 = 24 kWh * 0.5 = 12 kg
    // Readings on Jun 30 and Sep 1 fall outside both periods and must be ignored.
    private List<Reading> augustReadingsWithJuly(double... julyEnergy) {
        List<Reading> readings = new ArrayList<>();
        readings.add(new Reading(LocalDateTime.of(2026, 6, 30, 12, 0), 100, "energy"));  // outside both
        for (int i = 0; i < julyEnergy.length; i++) {
            readings.add(new Reading(LocalDateTime.of(2026, 7, 5 + i * 10, 12, 0), julyEnergy[i], "energy"));
        }
        readings.add(new Reading(LocalDateTime.of(2026, 8, 5, 12, 0), 10, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 20, 12, 0), 14, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 12, 0), 100, "energy"));   // outside both
        return readings;
    }

    private void useAugustWithEnergyRate() {
        evaluator.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59));
        evaluator.setEmissionRate("energy", 0.5);
    }

    @Test
    void shouldCalculatePreviousPeriodEmission() {
        // July: 8 + 12 = 20 kWh * 0.5 = 10 kg
        evaluator.setReadings(augustReadingsWithJuly(8, 12));
        useAugustWithEnergyRate();

        assertEquals(10.0, evaluator.calculatePreviousPeriodEmission("energy"));
    }

    @Test
    void shouldShowIncreaseFromPreviousPeriod() {
        // July 10 kg -> August 12 kg = +20%
        evaluator.setReadings(augustReadingsWithJuly(8, 12));
        useAugustWithEnergyRate();

        assertEquals(20.0, evaluator.getPercentageChange("energy"));
        assertEquals(EnvironmentalEvaluator.ChangeDirection.UP, evaluator.getChangeDirection("energy"));
    }

    @Test
    void shouldShowDecreaseFromPreviousPeriod() {
        // July: 32 kWh * 0.5 = 16 kg -> August 12 kg = -25%
        evaluator.setReadings(augustReadingsWithJuly(12, 20));
        useAugustWithEnergyRate();

        assertEquals(-25.0, evaluator.getPercentageChange("energy"));
        assertEquals(EnvironmentalEvaluator.ChangeDirection.DOWN, evaluator.getChangeDirection("energy"));
    }

    @Test
    void shouldShowNoChangeFromPreviousPeriod() {
        // July: 24 kWh * 0.5 = 12 kg -> August 12 kg = 0%
        evaluator.setReadings(augustReadingsWithJuly(10, 14));
        useAugustWithEnergyRate();

        assertEquals(0.0, evaluator.getPercentageChange("energy"));
        assertEquals(EnvironmentalEvaluator.ChangeDirection.NO_CHANGE, evaluator.getChangeDirection("energy"));
    }

    @Test
    void shouldHandleNoPreviousPeriodData() {
        // no July readings: nothing to compare against, and no dividing by zero
        evaluator.setReadings(augustReadingsWithJuly());
        useAugustWithEnergyRate();

        assertFalse(evaluator.hasPreviousPeriodData("energy"));
        assertEquals(0.0, evaluator.getPercentageChange("energy"));
    }

    //
    // Criteria: The user's footprint is compared against the State average and displayed together.
    //
    // The State average is mock data (kg CO2e per household per day) until real figures are sourced.
    // Tests set it explicitly so they don't depend on the mock value.

    // August: energy 24 kWh * 0.5 = 12 kg, water 26.4 L * 0.25 = 6.6 kg -> 18.6 kg total
    private void setUpAugustFootprint() {
        List<Reading> readings = new ArrayList<>();
        readings.add(new Reading(LocalDateTime.of(2026, 8, 5, 12, 0), 10, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 12, 12, 0), 26.4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 20, 12, 0), 14, "energy"));

        evaluator.setReadings(readings);
        evaluator.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59));
        evaluator.setEmissionRate("energy", 0.5);
        evaluator.setEmissionRate("water", 0.25);
    }

    @Test
    void shouldHaveMockStateAverageByDefault() {
        assertTrue(evaluator.getStateAverageKgPerDay() > 0);
    }

    @Test
    void shouldScaleStateAverageToPeriodLength() {
        setUpAugustFootprint();
        evaluator.setStateAverageKgPerDay(0.5);

        // 31 days * 0.5 kg = 15.5 kg
        assertEquals(15.5, evaluator.getStateAverageForPeriod());
    }

    @Test
    void shouldCompareFootprintAboveStateAverage() {
        setUpAugustFootprint();
        evaluator.setStateAverageKgPerDay(0.5);

        // 18.6 kg vs 15.5 kg = +20%
        assertEquals(18.6, evaluator.calculateEmissionPower("both"));
        assertEquals(20.0, evaluator.getPercentageVsStateAverage());
    }

    @Test
    void shouldCompareFootprintBelowStateAverage() {
        setUpAugustFootprint();
        evaluator.setStateAverageKgPerDay(0.75);

        // 31 * 0.75 = 23.25 kg; 18.6 kg vs 23.25 kg = -20%
        assertEquals(-20.0, evaluator.getPercentageVsStateAverage());
    }

    @Test
    void shouldNotAcceptNegativeStateAverage() {
        evaluator.setStateAverageKgPerDay(0.5);
        evaluator.setStateAverageKgPerDay(-1);

        assertEquals(0.5, evaluator.getStateAverageKgPerDay());
    }

}