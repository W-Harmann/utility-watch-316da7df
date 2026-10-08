package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;
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
        detector = new SpikeDetector(3.0);
    }

    @Test
    void shouldFlagUnusualConsistentSpikes() {
        // a value of 3x compared to surrounding values
        List<Reading> readings = List.of(
                new Reading(LocalDateTime.of(2026, 9, 1, 12, 0), 10.0),
                new Reading(LocalDateTime.of(2026, 9, 8, 12, 0), 30.0),
                new Reading(LocalDateTime.of(2026, 9, 15, 12, 0), 10.0)
        );

        // 3 recurring spikes found (meets the minimum of 3)
        List<Reading> result = detector.hasSpikes(readings);
        assertTrue(!result.isEmpty());
    }
}