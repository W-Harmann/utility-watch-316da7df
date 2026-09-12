package com.utilitywatch;

// US10.1 - As an environmentally conscious user I want the app to estimate the environmental impact
// so that I can understand the ecological/environmental footprint of my consumption and make more sustainable choices about utility.
//      [x] The footprint is broken down per utility type, and the per-utility figures sum to the total.
//      [x] The user can set a time range to measure impact in a range and the estimated CO₂ equivalent in kilograms for the selected period.
//      [?] The footprint is compared against the equivalent previous period, showing the percentage change and its direction.
//      [?] The users footprint is compared against the State average and displayed together.
//
// Notes: Unsure of how to "compare" of equivalent period, assuming the periods are free form (Year, Month, Day, Time())
//        rather than [Years() / Months() / Days()]   i.e. January, February, March, ...

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

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

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        evaluator.setReadings(readings);

        assertArrayEquals(
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

        assertArrayEquals(
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
        evaluator.setEmissionRate("power",0.6);

        assertEquals(
                0.6,
                evaluator.getEmissionRate("power")
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

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("power", 0.6);

        // Inclusive Power Reading (3 + 5) * Co2 (0.6) = 4.8
        assertEquals(
                4.8,
                evaluator.calculateEmissionPower("power")
        );
    }

    // Test 2
    @Test
    void shouldCalculateTimeRangeEmissionWater() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "power"));
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

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "power"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("power", 0.6);
        evaluator.setEmissionRate("water", 0.3);

        // Inclusive Power Reading (3 + 5) * Co2 (0.6) = 4.8
        // Inclusive Water Reading (4 + 3) * Co2 (0.3) = 2.1
        // Sum (4.8 + 2.1) = 6.9
        assertEquals(
                6.9,
                evaluator.calculateEmissionPower("both")
        );
    }
}