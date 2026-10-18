package com.utilitywatch;

// US10.3 - As an Environmentally Conscious user, I want to track how my environmental impact changes over
// time so that I can see whether my conservation efforts are actually reducing my footprint.
//
// Notes: These test the text and grouping behind the footprint chart on the Impact page
//        (axis labels, tooltips, the "no data" summary and where the line breaks).
//        The numbers themselves are tested in EnvironmentalEvaluatorTest (getFootprintTrend).

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import com.utilitywatch.EnvironmentalEvaluator.FootprintPoint;
import com.utilitywatch.EnvironmentalEvaluator.Grouping;

public class FootprintTrendFormatterTest {

    private static FootprintPoint point(LocalDate start, LocalDate end, Double kg) {
        return new FootprintPoint(start, end, kg == null ? null : BigDecimal.valueOf(kg));
    }

    private static LocalDate date(int year, int month, int day) {
        return LocalDate.of(year, month, day);
    }

    //
    // Axis labels: one per point, in order, and never repeated (repeated labels would merge points on the chart).
    //

    @Test
    void shouldLabelMonthsWithMonthAndYear() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 15), date(2026, 7, 31), 1.0),
                point(date(2026, 8, 1), date(2026, 8, 31), null),
                point(date(2026, 9, 1), date(2026, 9, 10), 2.0));

        assertEquals(List.of("Jul 2026", "Aug 2026", "Sep 2026"),
                FootprintTrendFormatter.labels(points, Grouping.MONTHLY));
    }

    @Test
    void shouldLabelWeeksByTheirFirstDay() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 1), date(2026, 7, 5), 1.0),     // first week cut to the range (Wed - Sun)
                point(date(2026, 7, 6), date(2026, 7, 12), null),
                point(date(2026, 7, 13), date(2026, 7, 14), 2.0));

        assertEquals(List.of("Jul 1", "Jul 6", "Jul 13"),
                FootprintTrendFormatter.labels(points, Grouping.WEEKLY));
    }

    @Test
    void shouldAddYearToWeekLabelsWhenRangeCrossesIntoAnotherYear() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 12, 21), date(2026, 12, 27), 1.0),
                point(date(2026, 12, 28), date(2027, 1, 3), 1.0),
                point(date(2027, 1, 4), date(2027, 1, 10), 1.0));

        assertEquals(List.of("Dec 21, 2026", "Dec 28, 2026", "Jan 4, 2027"),
                FootprintTrendFormatter.labels(points, Grouping.WEEKLY));
    }

    @Test
    void shouldNeverRepeatAWeekLabelOverMoreThanAYear() {
        // Jul 6 2026 and Jul 6 2027 must not both be labelled "Jul 6"
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 6), date(2026, 7, 12), 1.0),
                point(date(2027, 7, 5), date(2027, 7, 11), 1.0),
                point(date(2027, 7, 6), date(2027, 7, 6), 1.0));

        List<String> labels = FootprintTrendFormatter.labels(points, Grouping.WEEKLY);

        assertEquals(3, labels.stream().distinct().count());
    }

    @Test
    void shouldHaveNoLabelsWhenThereAreNoPoints() {
        assertEquals(List.of(), FootprintTrendFormatter.labels(List.of(), Grouping.WEEKLY));
    }

    //
    // Tooltip text: the exact dates of the period and its footprint, or "no readings".
    //

    @Test
    void shouldDescribePeriodWithFootprint() {
        FootprintPoint week = point(date(2026, 7, 6), date(2026, 7, 12), 12.345);

        assertEquals("Jul 6 – Jul 12, 2026: 12.35 kg CO₂e", FootprintTrendFormatter.describe(week));
    }

    @Test
    void shouldDescribeGenuineZeroAsZeroKg() {
        FootprintPoint week = point(date(2026, 7, 6), date(2026, 7, 12), 0.0);

        assertEquals("Jul 6 – Jul 12, 2026: 0.00 kg CO₂e", FootprintTrendFormatter.describe(week));
    }

    @Test
    void shouldDescribePeriodWithNoReadingsAsNoData() {
        FootprintPoint month = point(date(2026, 8, 1), date(2026, 8, 31), null);

        assertEquals("Aug 1 – Aug 31, 2026: no data (no readings)", FootprintTrendFormatter.describe(month));
    }

    @Test
    void shouldDescribeSingleDayPeriod() {
        FootprintPoint lastDay = point(date(2026, 7, 31), date(2026, 7, 31), 1.0);

        assertEquals("Jul 31, 2026: 1.00 kg CO₂e", FootprintTrendFormatter.describe(lastDay));
    }

    @Test
    void shouldShowBothYearsWhenPeriodCrossesIntoNewYear() {
        FootprintPoint week = point(date(2026, 12, 28), date(2027, 1, 3), 1.0);

        assertEquals("Dec 28, 2026 – Jan 3, 2027: 1.00 kg CO₂e", FootprintTrendFormatter.describe(week));
    }

    //
    // Line segments: the chart line stops at a period with no data instead of joining across it.
    //

    @Test
    void shouldSplitLineAtPeriodsWithNoData() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 1), date(2026, 7, 5), 1.0),
                point(date(2026, 7, 6), date(2026, 7, 12), 2.0),
                point(date(2026, 7, 13), date(2026, 7, 19), null),
                point(date(2026, 7, 20), date(2026, 7, 26), 0.0),    // a genuine zero still joins the line
                point(date(2026, 7, 27), date(2026, 7, 31), 3.0));

        assertEquals(List.of(List.of(0, 1), List.of(3, 4)), FootprintTrendFormatter.runsWithData(points));
    }

    @Test
    void shouldKeepSinglePointBetweenGapsAsItsOwnRun() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 1), date(2026, 7, 31), null),
                point(date(2026, 8, 1), date(2026, 8, 31), 4.0),
                point(date(2026, 9, 1), date(2026, 9, 30), null));

        assertEquals(List.of(List.of(1)), FootprintTrendFormatter.runsWithData(points));
    }

    @Test
    void shouldHaveNoRunsWhenNoPeriodHasData() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 1), date(2026, 7, 31), null),
                point(date(2026, 8, 1), date(2026, 8, 31), null));

        assertEquals(List.of(), FootprintTrendFormatter.runsWithData(points));
    }

    //
    // Summary under the chart: how many periods have no data.
    //

    @Test
    void shouldCountWeeksWithNoData() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 6), date(2026, 7, 12), 1.0),
                point(date(2026, 7, 13), date(2026, 7, 19), null),
                point(date(2026, 7, 20), date(2026, 7, 26), null));

        assertEquals("2 of 3 weeks have no readings", FootprintTrendFormatter.noDataSummary(points, Grouping.WEEKLY));
    }

    @Test
    void shouldUseSingularForOneMonthWithNoData() {
        List<FootprintPoint> points = List.of(
                point(date(2026, 7, 1), date(2026, 7, 31), 1.0),
                point(date(2026, 8, 1), date(2026, 8, 31), null),
                point(date(2026, 9, 1), date(2026, 9, 30), 0.0));   // zero is data, not "no readings"

        assertEquals("1 of 3 months has no readings", FootprintTrendFormatter.noDataSummary(points, Grouping.MONTHLY));
    }

    @Test
    void shouldSayWhenNoPeriodHasReadings() {
        List<FootprintPoint> points = List.of(point(date(2026, 7, 1), date(2026, 7, 31), null));

        assertEquals("No readings in the selected period", FootprintTrendFormatter.noDataSummary(points, Grouping.MONTHLY));
    }

    @Test
    void shouldHaveNoSummaryWhenEveryPeriodHasReadings() {
        List<FootprintPoint> points = List.of(point(date(2026, 7, 1), date(2026, 7, 31), 0.0));

        assertEquals("", FootprintTrendFormatter.noDataSummary(points, Grouping.MONTHLY));
    }
}
