package com.utilitywatch;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

class LongDurationDetectorTest {

    private LongDurationDetector detector;

    @BeforeEach
    void setUp() {
        // consumption baseline of 2.0
        // flagged if consumption usage stays above it for 6+ consecutive hours
        detector = new LongDurationDetector(2.0, 6);
    }

    @Test
    void shouldFlagUnusualLongConsumptionPeriod() {
        // 7 consecutive hourly readings of 5.0 (each above the 2.0 baseline)
        List<Reading> readings = List.of(
                new Reading(LocalDateTime.of(2026, 9, 10, 1, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 2, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 3, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 4, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 5, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 6, 0), 5.0),
                new Reading(LocalDateTime.of(2026, 9, 10, 7, 0), 5.0)
        );

        boolean result = detector.hasLongDurationUsage(readings);
        assertTrue(result);
    }


}