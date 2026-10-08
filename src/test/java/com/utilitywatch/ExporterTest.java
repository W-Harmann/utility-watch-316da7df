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
import java.time.LocalDate;
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

    // ---------------------------------------------------------------------------------------------
    // As a Household user, I want to select a custom date range before exporting so that I only
    // export the data relevant to my needs (e.g. one billing cycle, one year).
    //      [ ] Only readings inside the selected range are exported, in date order.
    //      [ ] The 'From' and 'To' days are both included in full.
    //      [ ] A single day (From = To) can be exported.
    //      [ ] A whole year can be exported, with the monthly average taken over 12 months.
    //      [ ] 'To' before 'From' is rejected with a message and no file.
    //      [ ] With no range selected, every reading is exported.
    //      [ ] The Period line shows the selected dates.
    //
    // Notes: These use setTimeRange(LocalDate, LocalDate), the whole-day version the date pickers call.
    //        Already covered above: utility + range together, and a range with no readings.
    // ---------------------------------------------------------------------------------------------

    private static Reading energyAt(String dateTime, double kWh) {
        return new Reading(LocalDateTime.parse(dateTime), kWh, "energy");
    }

    /** The Date column of the readings table in an exported CSV, in file order. */
    private static List<String> exportedDates(Path file) throws IOException {
        List<String> dates = new ArrayList<>();
        boolean inTable = false;
        for (String line : Files.readAllLines(file)) {
            if (line.equals("Date,Utility,Quantity,Unit")) {
                inTable = true;
            } else if (inTable) {
                if (line.isBlank()) {
                    break;
                }
                dates.add(line.split(",")[0]);
            }
        }
        return dates;
    }

    @Test
    void shouldExportOnlyReadingsInsideBillingCycle() throws IOException {
        exporter.setReadings(List.of(
                energyAt("2026-06-30T12:00", 5),
                energyAt("2026-07-10T12:00", 6),
                energyAt("2026-07-20T12:00", 7),
                energyAt("2026-08-01T12:00", 8)));
        exporter.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        Path file = tempDir.resolve("july.csv");

        Exporter.ExportResult result = exporter.export("energy", file);

        assertTrue(result.success(), result.message());
        assertEquals(List.of("2026-07-10", "2026-07-20"), exportedDates(file));
    }

    @Test
    void shouldSortExportedReadingsByDate() throws IOException {
        exporter.setReadings(List.of(
                energyAt("2026-07-20T12:00", 7),
                energyAt("2026-07-05T12:00", 5),
                energyAt("2026-07-12T12:00", 6)));
        exporter.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        Path file = tempDir.resolve("july.csv");

        exporter.export("energy", file);

        assertEquals(List.of("2026-07-05", "2026-07-12", "2026-07-20"), exportedDates(file));
    }

    @Test
    void shouldIncludeReadingAtMidnightOnFromDay() throws IOException {
        exporter.setReadings(List.of(
                energyAt("2026-06-30T23:59:59", 4),   // one second before the range
                energyAt("2026-07-01T00:00", 5)));
        exporter.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        Path file = tempDir.resolve("july.csv");

        Exporter.ExportResult result = exporter.export("energy", file);

        assertTrue(result.success(), result.message());
        assertEquals(List.of("2026-07-01"), exportedDates(file));
    }

    @Test
    void shouldIncludeReadingLaterOnToDay() throws IOException {
        exporter.setReadings(List.of(
                energyAt("2026-07-31T15:30", 5),      // afternoon of the last day
                energyAt("2026-08-01T00:00", 6)));    // first moment after the range
        exporter.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        Path file = tempDir.resolve("july.csv");

        Exporter.ExportResult result = exporter.export("energy", file);

        assertTrue(result.success(), result.message());
        assertEquals(List.of("2026-07-31"), exportedDates(file));
    }

    @Test
    void shouldExportSingleDay() throws IOException {
        exporter.setReadings(List.of(
                energyAt("2026-07-14T09:00", 1),
                energyAt("2026-07-15T09:00", 2),
                energyAt("2026-07-16T09:00", 3)));
        exporter.setTimeRange(LocalDate.of(2026, 7, 15), LocalDate.of(2026, 7, 15));
        Path file = tempDir.resolve("day.csv");

        Exporter.ExportResult result = exporter.export("energy", file);

        assertTrue(result.success(), result.message());
        assertEquals(List.of("2026-07-15"), exportedDates(file));
    }

    // 2025: energy (2 + 3 + 4) * 2.5 = 22.50, / 12 months = 1.875, rounds to 1.88
    @Test
    void shouldExportWholeYearAveragedOverTwelveMonths() throws IOException {
        exporter.setReadings(List.of(
                energyAt("2024-12-31T12:00", 1),
                energyAt("2025-01-01T08:00", 2),
                energyAt("2025-06-15T12:00", 3),
                energyAt("2025-12-31T20:00", 4),
                energyAt("2026-01-01T12:00", 5)));
        exporter.setTimeRange(LocalDate.of(2025, 1, 1), LocalDate.of(2025, 12, 31));
        exporter.setUtilityCost("energy", 2.5);
        Path file = tempDir.resolve("2025.csv");

        Exporter.ExportResult result = exporter.export("energy", file);
        String content = Files.readString(file);

        assertTrue(result.success(), result.message());
        assertEquals(List.of("2025-01-01", "2025-06-15", "2025-12-31"), exportedDates(file));
        assertTrue(content.contains("Total,,22.50"));
        assertTrue(content.contains("Average per month (12 months),,1.88"));
    }

    @Test
    void shouldRejectToBeforeFromWithMessageAndNoFile() {
        exporter.setReadings(sampleReadings());
        exporter.setTimeRange(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 8, 1));
        Path file = tempDir.resolve("report.csv");

        Exporter.ExportResult result = exporter.export("both", file);

        assertFalse(result.success());
        assertEquals("'To' date must be on or after 'From' date", result.message());
        assertFalse(Files.exists(file));
    }

    @Test
    void shouldExportEverythingWhenNoRangeSelected() throws IOException {
        exporter.setReadings(sampleReadings());   // no setTimeRange call
        Path file = tempDir.resolve("all.csv");

        Exporter.ExportResult result = exporter.export("both", file);

        assertTrue(result.success(), result.message());
        assertEquals(List.of("2026-08-01", "2026-08-10", "2026-08-17", "2026-09-01", "2026-09-03"),
                exportedDates(file));
    }

    @Test
    void shouldShowSelectedRangeInPeriodLine() throws IOException {
        // readings fall on Aug 10 and Sep 3, but the user picked Aug 5 - Sep 20
        exporter.setReadings(sampleReadings());
        exporter.setTimeRange(LocalDate.of(2026, 8, 5), LocalDate.of(2026, 9, 20));
        Path file = tempDir.resolve("report.csv");

        exporter.export("water", file);

        assertTrue(Files.readAllLines(file).contains("Period,2026-08-05,2026-09-20"));
    }
}
