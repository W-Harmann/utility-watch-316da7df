package com.utilitywatch.conservationAdvisor;

import com.utilitywatch.Reading;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

class ConservationAdvisorTest {

    private ConservationAdvisor advisor;

    @BeforeEach
    void setUp() {
        advisor = new ConservationAdvisor();
    }

    @Test
    void shouldCalculatePercentIncreaseInAverageUsage() {
        // previous usage period averaged 10.0

        List<Reading> previousPeriod = List.of(
                new Reading(LocalDateTime.of(2026, 9, 10, 1, 0), 10.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 2, 0), 10.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 3, 0), 10.0));

        // current usage period averaged 12.0
        List<Reading> currentPeriod = List.of(
                new Reading(LocalDateTime.of(2026, 9, 10, 1, 0), 12.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 2, 0), 12.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 3, 0), 12.0));

        Optional<Double> percentIncrease = advisor.calculatePercentChange(Optional.of(previousPeriod), currentPeriod);

        // a comparison should exist since previous period data was provided
        assertTrue(percentIncrease.isPresent());

        // 10.0 to 12.0 is a 20% increase
        // allow a small difference for rounding
        assertEquals(20.0, percentIncrease.get(), 0.01);
    }

    @Test
    void shouldNotCalculatePercentIncreaseWhenNoPreviousDataAvailable() {

        // current usage period averaged 12.0
        List<Reading> currentPeriod = List.of(
                new Reading(LocalDateTime.of(2026, 9, 10, 1, 0), 12.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 2, 0), 12.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 3, 0), 12.0));

        Optional<Double> percentIncrease = advisor.calculatePercentChange(Optional.empty(), currentPeriod);

        // no comparison should be made since there is nothing to compare against
        assertTrue(percentIncrease.isEmpty());
    }

    @Test
    void shouldCompareDurationAndHeightOfCommonPatterns() {
        // previous pattern was consumption of 5.0 across 4 readings
        List<Reading> previousPeriod = List.of(
                new Reading(LocalDateTime.of(2026, 9, 10, 1, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 2, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 3, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 4, 0), 5.0));

        // current pattern is consumption of 8.0 across 6 readings
        List<Reading> currentPeriod = List.of(
                new Reading(LocalDateTime.of(2026, 10, 10, 1, 0), 8.0),
                new Reading(LocalDateTime.of(2026, 10, 10, 2, 0), 8.0),
                new Reading(LocalDateTime.of(2026, 10, 10, 3, 0), 8.0),
                new Reading(LocalDateTime.of(2026, 10, 10, 4, 0), 8.0),
                new Reading(LocalDateTime.of(2026, 10, 10, 5, 0), 8.0),
                new Reading(LocalDateTime.of(2026, 10, 10, 6, 0), 8.0));

        long durationChange = advisor.compareDuration(previousPeriod, currentPeriod);
        double consumptionChange = advisor.compareConsumptionLevel(previousPeriod, currentPeriod);

        // check that the duration has increased (2 hours in seconds)
        assertEquals(7200, durationChange);

        // check that consumption increased 60% (8.0 compared to 5.0)
        assertEquals(60, consumptionChange);
    }
}