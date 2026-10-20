package com.utilitywatch.reports;

// US7.3 - As a user, I want to customise the information included in my consumption report so that I can focus on
// the information that is relevant to me.
//      [x] The user can select energy, water, or both.
//      [x] The user can select the reporting period.
//      [x] The user can select which available statistics or analysis results to include.
//      [x] The generated report only displays the information selected by the user.
//      [x] The report remains clearly organised when different information is selected.
//      [x] The system prevents the user from generating a report with no selected content.
//
// US11.2 - As a Household user, I want to select a custom date range before exporting so that I only export the
// data relevant to my needs (e.g. one billing cycle, one year).

import com.utilitywatch.Exporter.ExportResult;
import com.utilitywatch.Reading;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class CustomReportTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 20);
    private static final LocalDate JUL_1 = LocalDate.of(2026, 7, 1);
    private static final LocalDate SEP_30 = LocalDate.of(2026, 9, 30);
    private static final CustomReportBuilder.Rates RATES = new CustomReportBuilder.Rates(0.5, 0.01, 0.7, 0.001);

    private final CustomReportBuilder builder = new CustomReportBuilder();
    private final CustomReportWriter writer = new CustomReportWriter();

    /** Daily readings from 1 Apr to 30 Sep: energy 10 kWh/day, water 200 L/day. */
    private static List<Reading> sampleReadings() {
        List<Reading> readings = new ArrayList<>();
        for (LocalDate day = LocalDate.of(2026, 4, 1); !day.isAfter(SEP_30); day = day.plusDays(1)) {
            readings.add(new Reading(day.atTime(9, 0), 10, "energy"));
            readings.add(new Reading(day.atTime(9, 30), 200, "water"));
        }
        return readings;
    }

    private CustomReport build(String utility, LocalDate from, LocalDate to, Set<ReportSection> sections) {
        return builder.build(sampleReadings(), new ReportOptions(utility, from, to, sections), RATES, TODAY);
    }

    private static List<String> headings(CustomReport report) {
        return report.sections().stream().flatMap(s -> s.tables().stream()).map(CustomReport.Table::heading).toList();
    }

    // ---- preventing an empty report ----

    @Test
    void noSelectedContentIsInvalid() {
        ReportOptions options = new ReportOptions("both", JUL_1, SEP_30, Set.of());
        assertFalse(options.isValid());
        assertEquals("Select at least one item to include in your report", options.validate().get());
    }

    @Test
    void builderRefusesToGenerateReportWithNoSelectedContent() {
        assertThrows(IllegalArgumentException.class,
                () -> build("both", JUL_1, SEP_30, EnumSet.noneOf(ReportSection.class)));
    }

    @Test
    void nullSectionsAreTreatedAsNoneSelected() {
        assertFalse(new ReportOptions("both", JUL_1, SEP_30, null).isValid());
    }

    // ---- reporting period (US11.2) ----

    @Test
    void periodMustHaveBothDates() {
        assertFalse(new ReportOptions("both", null, SEP_30, EnumSet.allOf(ReportSection.class)).isValid());
        assertFalse(new ReportOptions("both", JUL_1, null, EnumSet.allOf(ReportSection.class)).isValid());
    }

    @Test
    void periodCannotEndBeforeItStarts() {
        ReportOptions options = new ReportOptions("both", SEP_30, JUL_1, EnumSet.of(ReportSection.COST_SUMMARY));
        assertEquals("The end date must be on or after the start date", options.validate().get());
    }

    @Test
    void singleDayPeriodIsValid() {
        ReportOptions options = new ReportOptions("energy", JUL_1, JUL_1, EnumSet.of(ReportSection.READINGS_DATA));
        assertTrue(options.isValid());
        assertEquals(1, options.days());
    }

    @Test
    void readingsDataOnlyIncludesReadingsInsideTheCustomRange() {
        // one billing month: 1 - 31 Aug (31 days x 2 utilities)
        CustomReport report = build("both", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                EnumSet.of(ReportSection.READINGS_DATA));
        List<List<String>> rows = report.sections().get(0).tables().get(0).rows();

        assertEquals(62, rows.size());
        assertEquals("2026-08-01", rows.get(0).get(0));
        assertEquals("2026-08-31", rows.get(rows.size() - 1).get(0));
    }

    @Test
    void costSummaryUsesOnlyTheCustomRange() {
        // Jul-Sep = 92 days: energy 920 kWh x $0.50 = $460.00, water 18400 L x $0.01 = $184.00
        CustomReport report = build("both", JUL_1, SEP_30, EnumSet.of(ReportSection.COST_SUMMARY));
        List<List<String>> rows = report.sections().get(0).tables().get(0).rows();

        assertEquals(List.of("Energy", "920.00 kWh", "$0.5/kWh", "$460.00"), rows.get(0));
        assertEquals(List.of("Water", "18400.0 L", "$0.01/L", "$184.00"), rows.get(1));
        assertEquals("$644.00", rows.get(2).get(3));
        // 3 calendar months
        assertEquals(List.of("Average per month (3 months)", "$214.67"),
                report.sections().get(0).tables().get(1).rows().get(1));
    }

    @Test
    void rangeWithNoReadingsIsReportedAndCannotBeSaved() throws IOException {
        CustomReport report = build("both", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                EnumSet.of(ReportSection.CONSUMPTION_STATISTICS));
        assertFalse(report.hasReadings());

        Path file = Files.createTempDirectory("report-test").resolve("empty.csv");
        ExportResult result = writer.write(report, file);
        assertFalse(result.success());
        assertFalse(Files.exists(file));
    }

    // ---- utility selection ----

    @Test
    void energyOnlyReportHasNoWaterContent() {
        CustomReport report = build("energy", JUL_1, SEP_30, EnumSet.of(
                ReportSection.CONSUMPTION_STATISTICS, ReportSection.COST_SUMMARY, ReportSection.READINGS_DATA));

        assertEquals("Energy", report.utilityLabel());
        assertFalse(headings(report).stream().anyMatch(h -> h.startsWith("Water")));
        String csv = writer.toCsv(report);
        assertFalse(csv.contains("Water"));
        assertFalse(csv.contains(" L,"));
    }

    @Test
    void waterOnlyReportHasNoEnergyContent() {
        CustomReport report = build("water", JUL_1, SEP_30, EnumSet.of(
                ReportSection.CONSUMPTION_STATISTICS, ReportSection.TRENDS_ANALYSIS, ReportSection.READINGS_DATA));

        assertEquals("Water", report.utilityLabel());
        assertFalse(writer.toCsv(report).contains("kWh"));
    }

    @Test
    void bothReportHasEnergyAndWater() {
        CustomReport report = build("both", JUL_1, SEP_30, EnumSet.of(ReportSection.CONSUMPTION_STATISTICS));
        assertEquals(List.of("Energy", "Water"), headings(report));
    }

    // ---- only selected sections, always in the same order ----

    @Test
    void reportOnlyContainsSelectedSections() {
        CustomReport report = build("both", JUL_1, SEP_30,
                EnumSet.of(ReportSection.COST_SUMMARY, ReportSection.ENVIRONMENTAL_IMPACT));

        assertEquals(List.of(ReportSection.COST_SUMMARY, ReportSection.ENVIRONMENTAL_IMPACT), report.sectionTypes());
        String text = writer.toText(report);
        assertTrue(text.contains("1. Cost Summary"));
        assertTrue(text.contains("2. Environmental Impact"));
        assertFalse(text.contains("Consumption Statistics"));
        assertFalse(text.contains("Readings Data"));
    }

    @Test
    void sectionsKeepReportOrderWhateverOrderTheyWerePicked() {
        Set<ReportSection> pickedBackwards = new java.util.LinkedHashSet<>(List.of(
                ReportSection.READINGS_DATA, ReportSection.COMPARISONS, ReportSection.CONSUMPTION_STATISTICS));
        CustomReport report = build("both", JUL_1, SEP_30, pickedBackwards);

        assertEquals(List.of(ReportSection.CONSUMPTION_STATISTICS, ReportSection.COMPARISONS,
                ReportSection.READINGS_DATA), report.sectionTypes());
    }

    @Test
    void everySectionCanBeGeneratedTogether() {
        CustomReport report = build("both", JUL_1, SEP_30, EnumSet.allOf(ReportSection.class));

        assertEquals(ReportSection.values().length, report.sections().size());
        for (CustomReport.Section section : report.sections()) {
            assertFalse(section.tables().isEmpty() && section.notes().isEmpty(), section.title() + " is empty");
        }
    }

    @Test
    void everySectionCanBeGeneratedOnItsOwn() {
        for (ReportSection type : ReportSection.values()) {
            for (String utility : List.of("energy", "water", "both")) {
                CustomReport report = build(utility, JUL_1, SEP_30, EnumSet.of(type));
                assertEquals(List.of(type), report.sectionTypes());
            }
        }
    }

    @Test
    void sectionsWithNoDataStillExplainWhy() {
        // a period with no readings: every section says so rather than being blank
        CustomReport report = build("both", LocalDate.of(2025, 1, 1), LocalDate.of(2025, 1, 31),
                EnumSet.allOf(ReportSection.class));
        for (CustomReport.Section section : report.sections()) {
            assertFalse(section.tables().isEmpty() && section.notes().isEmpty(), section.title() + " is blank");
        }
    }

    // ---- section content ----

    @Test
    void statisticsAreCalculatedForThePeriod() {
        CustomReport report = build("energy", JUL_1, SEP_30, EnumSet.of(ReportSection.CONSUMPTION_STATISTICS));
        List<List<String>> rows = report.sections().get(0).tables().get(0).rows();

        assertEquals(List.of("Readings", "92"), rows.get(0));
        assertEquals(List.of("Total", "920.00 kWh"), rows.get(1));
        assertEquals(List.of("Average per reading", "10.00 kWh"), rows.get(2));
        assertEquals(List.of("Variability", "Low"), rows.get(7));
    }

    @Test
    void comparisonsUseThePreviousPeriodOfTheSameLength() {
        CustomReport report = build("energy", JUL_1, SEP_30, EnumSet.of(ReportSection.COMPARISONS));
        CustomReport.Section section = report.sections().get(0);

        assertTrue(section.notes().get(0).contains("previous 92 days"));
        // same daily use in both periods -> 0% change in the average
        assertEquals("+0.0%", section.tables().get(0).rows().get(1).get(3));
    }

    @Test
    void trendsUseMonthsForLongRangesAndWeeksForShortOnes() {
        CustomReport quarter = build("energy", JUL_1, SEP_30, EnumSet.of(ReportSection.TRENDS_ANALYSIS));
        assertEquals("Month", quarter.sections().get(0).tables().get(0).columns().get(0));

        CustomReport month = build("energy", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31),
                EnumSet.of(ReportSection.TRENDS_ANALYSIS));
        assertEquals("Week", month.sections().get(0).tables().get(0).columns().get(0));
    }

    @Test
    void trendDescription() {
        assertEquals("Not enough data to show a trend", CustomReportBuilder.describeTrend(1, 10.0, 10.0));
        assertEquals("Steady (+2.0%)", CustomReportBuilder.describeTrend(3, 10.0, 10.2));
        assertEquals("Increasing (+50.0%)", CustomReportBuilder.describeTrend(3, 10.0, 15.0));
        assertEquals("Decreasing (-50.0%)", CustomReportBuilder.describeTrend(3, 10.0, 5.0));
    }

    @Test
    void bucketsCoverTheWholeRangeWithoutGaps() {
        List<LocalDate[]> months = CustomReportBuilder.buckets(LocalDate.of(2026, 7, 15), LocalDate.of(2026, 9, 10), false);
        assertEquals(3, months.size());
        assertEquals(LocalDate.of(2026, 7, 15), months.get(0)[0]);
        assertEquals(LocalDate.of(2026, 7, 31), months.get(0)[1]);
        assertEquals(LocalDate.of(2026, 9, 10), months.get(2)[1]);

        List<LocalDate[]> weeks = CustomReportBuilder.buckets(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31), true);
        assertEquals(5, weeks.size());
        assertEquals(LocalDate.of(2026, 8, 29), weeks.get(4)[0]);
    }

    @Test
    void unusualPatternsFindsASpike() {
        List<Reading> readings = new ArrayList<>();
        for (int day = 1; day <= 5; day++) {
            readings.add(new Reading(LocalDateTime.of(2026, 8, day, 12, 0), day == 3 ? 10 : 2, "energy"));
        }
        CustomReport report = builder.build(readings, new ReportOptions("energy", LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 5), EnumSet.of(ReportSection.UNUSUAL_PATTERNS)), RATES, TODAY);

        CustomReport.Table table = report.sections().get(0).tables().get(0);
        assertTrue(table.rows().stream().anyMatch(row -> row.get(1).equals("Spike") && row.get(0).startsWith("3 Aug")));
    }

    @Test
    void environmentalImpactShowsFootprintAndPreviousPeriodButNoStateAverage() {
        // the QLD average was only mock data, so it is not included in reports
        CustomReport report = build("both", JUL_1, SEP_30, EnumSet.of(ReportSection.ENVIRONMENTAL_IMPACT));
        String csv = writer.toCsv(report);

        // Jul-Sep: 920 kWh x 0.7 = 644 kg, 18400 L x 0.001 = 18.4 kg
        assertTrue(csv.contains("Total,,,662.40 kg CO2e"));
        assertTrue(csv.contains("Change from previous period"));
        assertFalse(csv.contains("QLD"));
    }

    @Test
    void conservationTipsAreUtilitySpecific() {
        assertTrue(CustomReportBuilder.tips("energy", 0, 0, 0).get(0).contains("hot water"));
        assertTrue(CustomReportBuilder.tips("water", 0, 0, 0).get(0).contains("shower"));
        assertEquals(4, CustomReportBuilder.tips("energy", 10, 5, 2).size());
    }

    // ---- writing the file ----

    @Test
    void savesCsvWithOnlySelectedSections() throws IOException {
        CustomReport report = build("both", JUL_1, SEP_30, EnumSet.of(ReportSection.COST_SUMMARY));
        Path file = Files.createTempDirectory("report-test").resolve("report.csv");

        ExportResult result = writer.write(report, file);
        assertTrue(result.success(), result.message());

        String csv = Files.readString(file);
        assertTrue(csv.contains("UtilityWatch Consumption Report"));
        assertTrue(csv.contains("Period,2026-07-01,2026-09-30"));
        assertTrue(csv.contains("COST SUMMARY"));
        assertFalse(csv.contains("READINGS DATA"));
        assertFalse(csv.contains("2026-08-15")); // no raw readings unless asked for
    }

    @Test
    void savesPlainTextWhenFileEndsInTxt() throws IOException {
        CustomReport report = build("energy", JUL_1, SEP_30, EnumSet.of(ReportSection.CONSUMPTION_STATISTICS));
        Path file = Files.createTempDirectory("report-test").resolve("report.txt");

        assertTrue(writer.write(report, file).success());
        String text = Files.readString(file);
        assertTrue(text.startsWith("UtilityWatch Consumption Report\n====="));
        assertTrue(text.contains("(92 days)"));
        assertFalse(text.contains(",")); // not CSV
    }

    @Test
    void reportNeverContainsAccountDetails() {
        List<Reading> readings = sampleReadings();
        readings.forEach(reading -> reading.setUserId(424242));
        CustomReport report = builder.build(readings,
                new ReportOptions("both", JUL_1, SEP_30, EnumSet.allOf(ReportSection.class)), RATES, TODAY);

        assertFalse(writer.toCsv(report).contains("424242"));
    }

    @Test
    void csvCellsWithCommasAreQuoted() {
        assertEquals("plain", CustomReportWriter.cell("plain"));
        assertEquals("\"a, b\"", CustomReportWriter.cell("a, b"));
        assertEquals("\"say \"\"hi\"\"\"", CustomReportWriter.cell("say \"hi\""));
    }
}
