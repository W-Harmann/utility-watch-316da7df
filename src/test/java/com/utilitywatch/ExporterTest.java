package com.utilitywatch;

// US11.1 - As a landlord I wish to share my usage data externally, to help potential new tenants understand their expected costs
//      [x] Readings for a selected utility type and date range export to a file at a user-chosen location.
//      [x] The export includes per-utility costs, the period total, the applied rates, and average cost per month across the covered period.
//      [x] Exported files contain no credentials, email address, or other personal identifiers.
//      [x] Exporting a period with no readings produces a message and no file.
//      [x] A failed write reports the reason and leaves no partial file behind.
//
// Notes: Average cost per month = period total / number of calendar months the range touches
//        (e.g. Aug 1 - Sep 30 = 2 months). Partial months count as whole months.
//        @TempDir gives each test its own temporary folder, deleted afterwards.

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ExporterTest {

    private Exporter exporter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        exporter = new Exporter();
    }

    private static List<Reading> sampleReadings() {
        List<Reading> readings = new ArrayList<>();
        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));
        return readings;
    }

    // Aug 1 - Sep 30: energy (3 + 5 + 3) * 2.5 = 27.50, water (4 + 2) * 1.5 = 9.00, total 36.50
    private void setUpAugustToSeptember() {
        exporter.setReadings(sampleReadings());
        exporter.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 9, 30, 23, 59));
        exporter.setUtilityCost("energy", 2.5);
        exporter.setUtilityCost("water", 1.5);
    }

    //
    // Readings and time range (shared behaviour, see CostBudgetEvaluatorTest)
    //

    @Test
    void shouldStoreReadings() {
        List<Reading> readings = sampleReadings();
        exporter.setReadings(readings);

        assertEquals(readings, exporter.getReadings());
    }

    @Test
    void shouldBeStoringTimeRange() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 8, 2, 0, 0);

        exporter.setTimeRange(fromDate, toDate);

        assertEquals(List.of(fromDate, toDate), exporter.getTimeRange());
    }

    @Test
    void shouldBeReturningInvalidTimeRange() {
        exporter.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 7, 1, 0, 0));

        assertFalse(exporter.getTimeRangeValidity());
    }

    //
    // Criteria: Readings for a selected utility type and date range export to a file at a user-chosen location.
    //

    @Test
    void shouldExportToChosenLocation() {
        setUpAugustToSeptember();
        Path file = tempDir.resolve("report.csv");

        Exporter.ExportResult result = exporter.export("both", file);

        assertTrue(result.success());
        assertTrue(Files.exists(file));
    }

    @Test
    void shouldOnlyExportSelectedUtilityWithinRange() throws IOException {
        exporter.setReadings(sampleReadings());
        exporter.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 9, 1, 0, 0));
        exporter.setUtilityCost("water", 1.5);
        Path file = tempDir.resolve("water.csv");

        exporter.export("water", file);
        String content = Files.readString(file);

        assertTrue(content.contains("2026-08-10"));   // water reading inside the range
        assertFalse(content.contains("2026-09-03"));  // water reading outside the range
        assertFalse(content.contains("energy"));      // other utility not selected
    }

    //
    // Criteria: The export includes per-utility costs, the period total, the applied rates,
    //           and average cost per month across the covered period.
    //

    @Test
    void shouldIncludeCostsRatesTotalAndMonthlyAverage() throws IOException {
        setUpAugustToSeptember();
        Path file = tempDir.resolve("report.csv");

        exporter.export("both", file);
        String content = Files.readString(file);

        assertTrue(content.contains("energy,2.5,27.50"));   // utility, applied rate, cost
        assertTrue(content.contains("water,1.5,9.00"));
        assertTrue(content.contains("Total,,36.50"));
        // 36.50 / 2 calendar months (Aug, Sep) = 18.25
        assertTrue(content.contains("Average per month (2 months),,18.25"));
    }

    //
    // Criteria: Exported files contain no credentials, email address, or other personal identifiers.
    //

    @Test
    void shouldNotIncludePersonalIdentifiers() throws IOException {
        List<Reading> readings = sampleReadings();
        for (Reading reading : readings) {
            reading.setUserId(987654);
        }
        exporter.setReadings(readings);
        exporter.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 9, 30, 23, 59));
        Path file = tempDir.resolve("report.csv");

        exporter.export("both", file);
        String content = Files.readString(file).toLowerCase();

        assertFalse(content.contains("987654"));
        assertFalse(content.contains("user"));
        assertFalse(content.contains("email"));
        assertFalse(content.contains("password"));
    }

    //
    // Criteria: Exporting a period with no readings produces a message and no file.
    //

    @Test
    void shouldNotCreateFileWhenNoReadingsInPeriod() {
        exporter.setReadings(sampleReadings());
        exporter.setTimeRange(LocalDateTime.of(2025, 1, 1, 0, 0), LocalDateTime.of(2025, 1, 31, 23, 59));
        Path file = tempDir.resolve("report.csv");

        Exporter.ExportResult result = exporter.export("both", file);

        assertFalse(result.success());
        assertEquals("No readings in the selected period", result.message());
        assertFalse(Files.exists(file));
    }

    //
    // Criteria: A failed write reports the reason and leaves no partial file behind.
    //

    @Test
    void shouldReportReasonWhenFolderDoesNotExist() throws IOException {
        setUpAugustToSeptember();
        Path file = tempDir.resolve("missing-folder").resolve("report.csv");

        Exporter.ExportResult result = exporter.export("both", file);

        assertFalse(result.success());
        assertFalse(result.message().isBlank());
        assertFalse(Files.exists(file));
        try (var files = Files.list(tempDir)) {
            assertEquals(0, files.count());   // nothing left behind
        }
    }

    @Test
    void shouldLeaveNoTemporaryFileWhenWriteFails() throws IOException {
        setUpAugustToSeptember();
        // the chosen path is a folder that already has something in it, so the final write fails
        Path file = tempDir.resolve("report.csv");
        Files.createDirectory(file);
        Files.writeString(file.resolve("keep.txt"), "existing");

        Exporter.ExportResult result = exporter.export("both", file);

        assertFalse(result.success());
        assertFalse(result.message().isBlank());
        try (var files = Files.list(tempDir)) {
            assertEquals(1, files.count());   // only the original folder, no leftover temp file
        }
    }
}
