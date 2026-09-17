package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalDateTime;
import java.time.LocalTime;


class ActiveHoursDetectorTest {

    private ActiveHoursDetector detector;

    @BeforeEach
    void setUp() {
        // assumes normal active hours are 6am to 11pm
        detector = new ActiveHoursDetector(LocalTime.of(6,0), LocalTime.of(23, 0));
    }

    @Test
    void shouldFlagHighConsumptionOutsideActiveTimes() {
        // reading of 8.0 at 2am (outside active hours)
        Reading current = new Reading(LocalDateTime.of(2026, 9, 10, 2, 0), 8.0);

        // normal overnight usage is around 1.0, so 8.0 is unusually high
        double normalOvernightBaseline = 1.0;

        boolean result = detector.isUnusual(current, normalOvernightBaseline);
        assertTrue(result);
    }

    @Test
    void shouldNotFlagNormalConsumptionOutsideActiveTimes() {
        // reading of 1.1 at 2am, outside active hours but close to the normal baseline
        Reading current = new Reading(LocalDateTime.of(2026, 9, 11, 2, 0), 1.1);
        double normalOvernightBaseline = 1.0;

        boolean result = detector.isUnusual(current, normalOvernightBaseline);
        assertFalse(result);
    }
}