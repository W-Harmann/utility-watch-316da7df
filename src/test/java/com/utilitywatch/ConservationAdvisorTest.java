package com.utilitywatch;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

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
}