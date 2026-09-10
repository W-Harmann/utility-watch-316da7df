package com.utilitywatch;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
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
        // current usage period averaged 12.0
        List<Double> previousPeriod = List.of(10.0, 10.0, 10.0);
        List<Double> currentPeriod = List.of(12.0, 12.0, 12.0);

        double percentIncrease = advisor.calculatePercentChange(previousPeriod, currentPeriod);

        // 10.0 to 12.0 is a 20% increase
        // allow a small difference for rounding
        assertEquals(20.0, percentIncrease, 0.01);
    }

    @Test
    void shouldCompareDurationAndHeightOfCommonPatterns() {
        // previous pattern was consumption of 5.0 across 4 readings
        List<Double> previousPeriod = List.of(5.0, 5.0, 5.0, 5.0);

        // current pattern is consumption of 8.0 across 6 readings
        List<Double> currentPeriod = List.of(8.0, 8.0, 8.0, 8.0, 8.0, 8.0);

        int readingCountChange = advisor.compareDuration(previousPeriod, currentPeriod);
        double consumptionChange = advisor.compareConsumptionLevel(previousPeriod, currentPeriod);

        // check that the number of readings increased (6 readings compared to 4)
        assertTrue(readingCountChange > 0);

        // check that consumption increased (8.0 compared to 5.0)
        assertTrue(consumptionChange > 0);
    }

    @Test
    void shouldCompareUsageToPreviousSeasonWhenAvailable() {
        // previous season average consumption was 10.0
        List<Double> previousSeason = List.of(10.0, 10.0, 10.0);

        // optional.of() represents previous season data being available
        Optional<Double> seasonalComparison = advisor.compareToSeason(Optional.of(previousSeason), currentSeason);

        // a comparison should exist since previous season data was provided
        assertTrue(seasonalComparison.isPresent());

        // 10.0 to 13.0 is a 30% increase (allow small margin for rounding)
        assertEquals(30.0, seasonalComparison.get(), 0.01);
    }

    @Test
    void shouldNotCompareToSeasonWhenNoPreviousSeasonDataAvailable() {
        // this season average consumption is 13.0
        List<Double> currentSeason = List.of(13.0, 13.0, 13.0);

        // optional.empty() represents no previous season data being available at all
        Optional<Double> seasonalComparison = advisor.compareToSeason(Optional.empty(), currentSeason);

        // no comparison should be made since there is nothing to compare against
        assertTrue(seasonalComparison.isEmpty());
    }
}