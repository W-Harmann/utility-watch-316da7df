package com.utilitywatch;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class UnusualConsumptionDetectorTest {

    private UnusualConsumptionDetector detector;
    @BeforeEach
    void setUp() {
        detector = new UnusualConsumptionDetector();
    }

    @Test
    void shouldFlagConsumptionMoreThanTenPercentAboveWeeklyMaximum() {
        // previous weeks normal readings
        List<Double> weeklyReadings = List.of(10.0, 12.0, 11.0, 15.0, 9.0, 11.0, 12.0);

        // weekly max is 15.0, 10% above that is 16.5, so 18.0 should be flagged
        double currentConsumption = 18.0;

        boolean result = detector.isUnusual(currentConsumption, weeklyReadings);
        assertTrue(result);
    }

    @Test
    void shouldFlagConsumptionMoreThanTenPercentBelowWeeklyMinimum() {
        List<Double> weeklyReadings = List.of(10.0, 12.0, 11.0, 15.0, 9.0, 11.0, 12.0);

        // weekly min is 9.0, 10% below that is 8.1, so 7.0 should be flagged
        double currentConsumption = 7.0;

        boolean result = detector.isUnusual(currentConsumption, weeklyReadings);
        assertTrue(result);
    }

    @Test
    void shouldNotFlagConsumptionWithinNormalRange() {
        List<Double> weeklyReadings = List.of(10.0, 12.0, 11.0, 15.0, 9.0, 11.0, 12.0);
        assertFalse(detector.isUnusual(11.5, weeklyReadings));
    }

    @Test
    void shouldNotFlagConsumptionExactlyAtTenPercentBoundary() {
        List<Double> weeklyReadings = List.of(10.0, 12.0, 11.0, 15.0, 9.0, 11.0, 12.0);
        // exactly 10% above max (16.5) - not unusual since unusual range is more than 10%
        assertFalse(detector.isUnusual(16.5, weeklyReadings));
    }
}