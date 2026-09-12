package com.utilitywatch;

// US11.1 - As a landlord I wish to share my usage data externally, to help potential new tenants understand their expected costs
//      [x] Readings for a selected utility type and date range export to a file at a user-chosen location.
//      [?] The export includes per-utility costs, the period total, the applied rates, and average cost per month across the covered period.
//      [?] Exported files contain no credentials, email address, or other personal identifiers.
//      [?] Exporting a period with no readings produces a message and no file.
//      [?] A failed write reports the reason and leaves no partial file behind.
//
// Notes: Not sure how to express the various criterias in TEST form

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;

public class ExporterTest {

    private Exporter exporter;

    @BeforeEach
    void setUp() {
        exporter = new Exporter();
    }

        //
        // Criteria: Readings for a selected utility type and date range export to a file at a user-chosen location.
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

    @Test
    void shouldStorePreferredExportLocation() {

        // store to class
        evaluator.setExportLocation("Documents");

        assertEquals(
                "Documents",
                evaluator.getExportLocation()
        );
    }

}