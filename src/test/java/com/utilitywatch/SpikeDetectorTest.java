package com.utilitywatch;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.util.List;

class SpikeDetectorTest {

    private SpikeDetector detector;

    @BeforeEach
    void setUp() {
        // baseline of 3.0
        // flagged if 3+ recurring spikes above it are found
        detector = new SpikeDetector(3.0, 3);
    }

    @Test
    void shouldFlagUnusualConsistentSpikes() {
        // same time each week, 3 readings of 10.0
        // each reading spikes above the baseline of 3.0
        List<Reading> readings = List.of(
                new Reading(LocalDateTime.of(2026, 9, 1, 12, 0), 10.0),
                new Reading(LocalDateTime.of(2026, 9, 8, 12, 0), 10.0),
                new Reading(LocalDateTime.of(2026, 9, 15, 12, 0), 10.0)
        );

        // 3 recurring spikes found (meets the minimum of 3)
        boolean result = detector.hasConsistentSpikes(readings);
        assertTrue(result);
    }
}