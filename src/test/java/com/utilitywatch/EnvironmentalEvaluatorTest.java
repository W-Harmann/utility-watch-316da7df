package com.utilitywatch;

// US10.1 - As an environmentally conscious user I want the app to estimate the environmental impact
// so that I can understand the ecological/environmental footprint of my consumption and make more sustainable choices about utility.
//      [x] The footprint is broken down per utility type, and the per-utility figures sum to the total.
//      [x] The user can set a time range to measure impact in a range and the estimated CO₂ equivalent in kilograms for the selected period.
//      [x] The footprint is compared against the equivalent previous period, showing the percentage change and its direction.
//      [x] The users footprint is compared against the State average and displayed together.
//


import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.ArrayList;
import java.util.List;
import java.time.LocalDate;
import java.time.LocalDateTime;
import com.utilitywatch.EnvironmentalEvaluator.FootprintPoint;
import com.utilitywatch.EnvironmentalEvaluator.Grouping;

public class EnvironmentalEvaluatorTest {

    private EnvironmentalEvaluator evaluator;

    @BeforeEach
    void setUp() {
        evaluator = new EnvironmentalEvaluator();
    }

    //
    // Criteria: The user can set a time range to measure impact in a range and the estimated CO₂ equivalent in kilograms for the selected period.
    //

    // See *CostBudgetEvaluatorTest (for the follow test details for Readings and TimeRange)
    // -- Because to calculate emissions, the app needs to know the readings

    @Test
    void shouldStoreReadings() {

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        evaluator.setReadings(readings);

        assertEquals(
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

        assertEquals(
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

    // Test Power
    @Test
    void shouldStoreEmissionByPower() {

        // store to class | assuming 1kWh produces 0.6 kg of CO2
        evaluator.setEmissionRate("energy",0.6);

        assertEquals(
                0.6,
                evaluator.getEmissionRate("energy")
        );
    }

    // Test Water
    @Test
    void shouldStoreEmissionByWater() {

        // store to class | assuming 1kLh produces 0.3 kg of CO2
        evaluator.setEmissionRate("water",0.3);

        assertEquals(
                0.3,
                evaluator.getEmissionRate("water")
        );
    }

    @Test
    void shouldNotAcceptNegativeEmissionRate() {
        evaluator.setEmissionRate("energy", 0.6);

        assertFalse(evaluator.setEmissionRate("energy", -1));   // rejected
        assertEquals(0.6, evaluator.getEmissionRate("energy"));  // old rate kept
    }

    // Test 1
    @Test
    void shouldCalculateTimeRangeEmissionPower() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("energy", 0.6);

        // Inclusive Power Reading (3 + 5) * Co2 (0.6) = 4.8
        assertEquals(
                4.8,
                evaluator.calculateEmissionPower("energy")
        );
    }

    // Test 2
    @Test
    void shouldCalculateTimeRangeEmissionWater() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("water", 0.3);

        // Inclusive Water Reading (4 + 3) * Co2 (0.3) = 2.1
        assertEquals(
                2.1,
                evaluator.calculateEmissionPower("water")
        );
    }

    // Test 3
    @Test
    void shouldCalculateTimeRangeEmissionBoth() {
        LocalDateTime fromDate = LocalDateTime.of(2026, 8, 1, 0, 0);
        LocalDateTime toDate = LocalDateTime.of(2026, 9, 1, 0, 0);

        List<Reading> readings = new ArrayList<>();

        readings.add(new Reading(LocalDateTime.of(2026, 8, 1, 0, 0), 3, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 10, 0, 0), 4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 17, 0, 0), 5, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 0, 0), 3, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 3, 0, 0), 2, "water"));

        // store to class
        evaluator.setReadings(readings);
        evaluator.setTimeRange(fromDate, toDate);

        evaluator.setEmissionRate("energy", 0.6);
        evaluator.setEmissionRate("water", 0.3);

        // Inclusive Power Reading (3 + 5) * Co2 (0.6) = 4.8
        // Inclusive Water Reading (4 + 3) * Co2 (0.3) = 2.1
        // Sum (4.8 + 2.1) = 6.9
        assertEquals(
                6.9,
                evaluator.calculateEmissionPower("both")
        );
    }

    //
    // Criteria: The footprint is compared against the equivalent previous period,
    //           showing the percentage change and its direction.
    //
    // The previous period is the same number of whole days immediately before the selected range,
    // e.g. Aug 1 - Aug 31 (31 days) is compared with Jul 1 - Jul 31.

    // Current period (August): energy 10 + 14 = 24 kWh * 0.5 = 12 kg
    // Readings on Jun 30 and Sep 1 fall outside both periods and must be ignored.
    private List<Reading> augustReadingsWithJuly(double... julyEnergy) {
        List<Reading> readings = new ArrayList<>();
        readings.add(new Reading(LocalDateTime.of(2026, 6, 30, 12, 0), 100, "energy"));  // outside both
        for (int i = 0; i < julyEnergy.length; i++) {
            readings.add(new Reading(LocalDateTime.of(2026, 7, 5 + i * 10, 12, 0), julyEnergy[i], "energy"));
        }
        readings.add(new Reading(LocalDateTime.of(2026, 8, 5, 12, 0), 10, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 20, 12, 0), 14, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 9, 1, 12, 0), 100, "energy"));   // outside both
        return readings;
    }

    private void useAugustWithEnergyRate() {
        evaluator.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59));
        evaluator.setEmissionRate("energy", 0.5);
    }

    @Test
    void shouldCalculatePreviousPeriodEmission() {
        // July: 8 + 12 = 20 kWh * 0.5 = 10 kg
        evaluator.setReadings(augustReadingsWithJuly(8, 12));
        useAugustWithEnergyRate();

        assertEquals(10.0, evaluator.calculatePreviousPeriodEmission("energy"));
    }

    @Test
    void shouldShowIncreaseFromPreviousPeriod() {
        // July 10 kg -> August 12 kg = +20%
        evaluator.setReadings(augustReadingsWithJuly(8, 12));
        useAugustWithEnergyRate();

        assertEquals(20.0, evaluator.getPercentageChange("energy"));
        assertEquals(EnvironmentalEvaluator.ChangeDirection.UP, evaluator.getChangeDirection("energy"));
    }

    @Test
    void shouldShowDecreaseFromPreviousPeriod() {
        // July: 32 kWh * 0.5 = 16 kg -> August 12 kg = -25%
        evaluator.setReadings(augustReadingsWithJuly(12, 20));
        useAugustWithEnergyRate();

        assertEquals(-25.0, evaluator.getPercentageChange("energy"));
        assertEquals(EnvironmentalEvaluator.ChangeDirection.DOWN, evaluator.getChangeDirection("energy"));
    }

    @Test
    void shouldShowNoChangeFromPreviousPeriod() {
        // July: 24 kWh * 0.5 = 12 kg -> August 12 kg = 0%
        evaluator.setReadings(augustReadingsWithJuly(10, 14));
        useAugustWithEnergyRate();

        assertEquals(0.0, evaluator.getPercentageChange("energy"));
        assertEquals(EnvironmentalEvaluator.ChangeDirection.NO_CHANGE, evaluator.getChangeDirection("energy"));
    }

    @Test
    void shouldHandleNoPreviousPeriodData() {
        // no July readings: nothing to compare against, and no dividing by zero
        evaluator.setReadings(augustReadingsWithJuly());
        useAugustWithEnergyRate();

        assertFalse(evaluator.hasPreviousPeriodData("energy"));
        assertEquals(0.0, evaluator.getPercentageChange("energy"));
    }

    //
    // Criteria: The user's footprint is compared against the State average and displayed together.
    //
    // The State average is mock data (kg CO2e per household per day) until real figures are sourced.
    // Tests set it explicitly so they don't depend on the mock value.

    // August: energy 24 kWh * 0.5 = 12 kg, water 26.4 L * 0.25 = 6.6 kg -> 18.6 kg total
    private void setUpAugustFootprint() {
        List<Reading> readings = new ArrayList<>();
        readings.add(new Reading(LocalDateTime.of(2026, 8, 5, 12, 0), 10, "energy"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 12, 12, 0), 26.4, "water"));
        readings.add(new Reading(LocalDateTime.of(2026, 8, 20, 12, 0), 14, "energy"));

        evaluator.setReadings(readings);
        evaluator.setTimeRange(LocalDateTime.of(2026, 8, 1, 0, 0), LocalDateTime.of(2026, 8, 31, 23, 59));
        evaluator.setEmissionRate("energy", 0.5);
        evaluator.setEmissionRate("water", 0.25);
    }

    @Test
    void shouldHaveMockStateAverageByDefault() {
        assertTrue(evaluator.getStateAverageKgPerDay() > 0);
    }

    @Test
    void shouldScaleStateAverageToPeriodLength() {
        setUpAugustFootprint();
        evaluator.setStateAverageKgPerDay(0.5);

        // 31 days * 0.5 kg = 15.5 kg
        assertEquals(15.5, evaluator.getStateAverageForPeriod());
    }

    @Test
    void shouldCompareFootprintAboveStateAverage() {
        setUpAugustFootprint();
        evaluator.setStateAverageKgPerDay(0.5);

        // 18.6 kg vs 15.5 kg = +20%
        assertEquals(18.6, evaluator.calculateEmissionPower("both"));
        assertEquals(20.0, evaluator.getPercentageVsStateAverage());
    }

    @Test
    void shouldCompareFootprintBelowStateAverage() {
        setUpAugustFootprint();
        evaluator.setStateAverageKgPerDay(0.75);

        // 31 * 0.75 = 23.25 kg; 18.6 kg vs 23.25 kg = -20%
        assertEquals(-20.0, evaluator.getPercentageVsStateAverage());
    }

    @Test
    void shouldNotAcceptNegativeStateAverage() {
        evaluator.setStateAverageKgPerDay(0.5);
        assertFalse(evaluator.setStateAverageKgPerDay(-1));   // rejected

        assertEquals(0.5, evaluator.getStateAverageKgPerDay());
    }

    // ---------------------------------------------------------------------------------------------
    // As an Environmentally Conscious user, I want to track how my environmental impact changes over
    // time so that I can see whether my conservation efforts are actually reducing my footprint.
    //      [ ] The footprint is shown as a chart over the selected range, with one point per week or month.
    //      [ ] The user can choose weekly or monthly grouping.
    //      [ ] The chart follows the utility selection (energy, water or both).
    //      [ ] Periods with no readings are shown as "no data", clearly distinguished from a genuine zero footprint.
    //
    // Notes: These test the data behind the chart (getFootprintTrend), not the chart itself.
    //        Weeks run Monday to Sunday. The first and last periods are cut to fit the selected range.
    //        "No data" is a point whose kg is null, so it can never be confused with 0 kg.
    // ---------------------------------------------------------------------------------------------

    // Jul - Sep 2026, energy at 0.5 kg/kWh, water at 0.25 kg/L
    //   July:      energy (10 + 6) * 0.5 = 8 kg,  water 20 * 0.25 = 5 kg  -> both 13 kg
    //   August:    no readings                                            -> no data
    //   September: energy 4 * 0.5 = 2 kg,         water 8 * 0.25 = 2 kg   -> both 4 kg
    // Readings on Jun 30 and Oct 1 fall outside the range and must be ignored.
    private void setUpJulyToSeptember() {
        evaluator.setReadings(List.of(
                new Reading(LocalDateTime.of(2026, 6, 30, 12, 0), 100, "energy"),   // outside
                new Reading(LocalDateTime.of(2026, 7, 3, 12, 0), 10, "energy"),
                new Reading(LocalDateTime.of(2026, 7, 10, 12, 0), 20, "water"),
                new Reading(LocalDateTime.of(2026, 7, 20, 12, 0), 6, "energy"),
                new Reading(LocalDateTime.of(2026, 9, 8, 12, 0), 4, "energy"),
                new Reading(LocalDateTime.of(2026, 9, 15, 12, 0), 8, "water"),
                new Reading(LocalDateTime.of(2026, 10, 1, 12, 0), 100, "energy"))); // outside
        evaluator.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 9, 30));
        evaluator.setEmissionRate("energy", 0.5);
        evaluator.setEmissionRate("water", 0.25);
    }

    private static List<LocalDate> starts(List<FootprintPoint> points) {
        return points.stream().map(FootprintPoint::periodStart).toList();
    }

    private static List<LocalDate> ends(List<FootprintPoint> points) {
        return points.stream().map(FootprintPoint::periodEnd).toList();
    }

    /** kg as a double for easy comparison; only call this on a point that has data. */
    private static double kg(FootprintPoint point) {
        return point.kg().doubleValue();
    }

    //
    // Criteria: The footprint is shown as a chart over the selected range, with one point per week or month.
    //

    @Test
    void shouldHaveOnePointPerMonthInRange() {
        setUpJulyToSeptember();

        List<FootprintPoint> trend = evaluator.getFootprintTrend("both", Grouping.MONTHLY);

        assertEquals(List.of(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)),
                starts(trend));
        assertEquals(List.of(LocalDate.of(2026, 7, 31), LocalDate.of(2026, 8, 31), LocalDate.of(2026, 9, 30)),
                ends(trend));
    }

    @Test
    void shouldHaveOnePointPerWeekInRange() {
        evaluator.setReadings(List.of(new Reading(LocalDateTime.of(2026, 7, 8, 12, 0), 1, "energy")));
        evaluator.setTimeRange(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 26));   // Mon Jul 6 - Sun Jul 26

        List<FootprintPoint> trend = evaluator.getFootprintTrend("energy", Grouping.WEEKLY);

        assertEquals(List.of(LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 13), LocalDate.of(2026, 7, 20)),
                starts(trend));
        assertEquals(List.of(LocalDate.of(2026, 7, 12), LocalDate.of(2026, 7, 19), LocalDate.of(2026, 7, 26)),
                ends(trend));
    }

    @Test
    void shouldCutFirstAndLastMonthToFitRange() {
        setUpJulyToSeptember();
        evaluator.setTimeRange(LocalDate.of(2026, 7, 15), LocalDate.of(2026, 9, 10));

        List<FootprintPoint> trend = evaluator.getFootprintTrend("energy", Grouping.MONTHLY);

        assertEquals(List.of(LocalDate.of(2026, 7, 15), LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 1)),
                starts(trend));
        assertEquals(LocalDate.of(2026, 9, 10), trend.get(2).periodEnd());
        assertEquals(3.0, kg(trend.getFirst()));   // only the Jul 20 reading: 6 * 0.5 (Jul 3 is before the range)
        assertEquals(2.0, kg(trend.get(2)));   // Sep 8: 4 * 0.5
    }

    @Test
    void shouldStartFirstWeekOnRangeStartWhenItIsMidWeek() {
        evaluator.setReadings(List.of(new Reading(LocalDateTime.of(2026, 7, 2, 12, 0), 1, "energy")));
        evaluator.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 14));   // Wed Jul 1 - Tue Jul 14

        List<FootprintPoint> trend = evaluator.getFootprintTrend("energy", Grouping.WEEKLY);

        // Wed 1 - Sun 5, Mon 6 - Sun 12, Mon 13 - Tue 14
        assertEquals(List.of(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 6), LocalDate.of(2026, 7, 13)),
                starts(trend));
        assertEquals(List.of(LocalDate.of(2026, 7, 5), LocalDate.of(2026, 7, 12), LocalDate.of(2026, 7, 14)),
                ends(trend));
    }

    @Test
    void shouldIncludeReadingLaterOnLastDayOfRange() {
        evaluator.setReadings(List.of(new Reading(LocalDateTime.of(2026, 7, 31, 18, 30), 10, "energy")));
        evaluator.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));
        evaluator.setEmissionRate("energy", 0.5);

        List<FootprintPoint> trend = evaluator.getFootprintTrend("energy", Grouping.MONTHLY);

        assertTrue(trend.getFirst().hasData());
        assertEquals(5.0, kg(trend.getFirst()));
    }

    @Test
    void shouldRejectTrendWithoutValidRange() {
        evaluator.setTimeRange(LocalDate.of(2026, 9, 30), LocalDate.of(2026, 7, 1));

        assertThrows(IllegalStateException.class,
                () -> evaluator.getFootprintTrend("both", Grouping.MONTHLY));
    }

    //
    // Criteria: The user can choose weekly or monthly grouping.
    //

    @Test
    void shouldGroupSameReadingsWeeklyOrMonthly() {
        setUpJulyToSeptember();
        evaluator.setTimeRange(LocalDate.of(2026, 7, 1), LocalDate.of(2026, 7, 31));

        List<FootprintPoint> monthly = evaluator.getFootprintTrend("both", Grouping.MONTHLY);
        List<FootprintPoint> weekly = evaluator.getFootprintTrend("both", Grouping.WEEKLY);

        assertEquals(1, monthly.size());
        assertEquals(13.0, kg(monthly.getFirst()));

        // July 2026 in weeks: Jul 1-5, 6-12, 13-19, 20-26, 27-31
        assertEquals(5, weekly.size());
        assertEquals(5.0, kg(weekly.getFirst()));     // Jul 3 energy 10 * 0.5
        assertEquals(5.0, kg(weekly.get(1)));     // Jul 10 water 20 * 0.25
        assertFalse(weekly.get(2).hasData());     // Jul 13-19: nothing
        assertEquals(3.0, kg(weekly.get(3)));     // Jul 20 energy 6 * 0.5
        assertFalse(weekly.get(4).hasData());     // Jul 27-31: nothing
    }

    @Test
    void shouldKeepWeekTogetherAcrossMonthBoundary() {
        evaluator.setReadings(List.of(
                new Reading(LocalDateTime.of(2026, 6, 30, 12, 0), 2, "energy"),
                new Reading(LocalDateTime.of(2026, 7, 2, 12, 0), 4, "energy")));
        evaluator.setTimeRange(LocalDate.of(2026, 6, 29), LocalDate.of(2026, 7, 5));   // Mon Jun 29 - Sun Jul 5
        evaluator.setEmissionRate("energy", 0.5);

        List<FootprintPoint> weekly = evaluator.getFootprintTrend("energy", Grouping.WEEKLY);
        List<FootprintPoint> monthly = evaluator.getFootprintTrend("energy", Grouping.MONTHLY);

        assertEquals(1, weekly.size());           // one week, spanning June and July
        assertEquals(3.0, kg(weekly.getFirst()));     // (2 + 4) * 0.5
        assertEquals(2, monthly.size());          // the same days split into June and July
        assertEquals(1.0, kg(monthly.getFirst()));
        assertEquals(2.0, kg(monthly.get(1)));
    }

    //
    // Criteria: The chart follows the utility selection (energy, water or both).
    //

    @Test
    void shouldFollowEnergySelection() {
        setUpJulyToSeptember();

        List<FootprintPoint> trend = evaluator.getFootprintTrend("energy", Grouping.MONTHLY);

        assertEquals(8.0, kg(trend.getFirst()));
        assertEquals(2.0, kg(trend.get(2)));
    }

    @Test
    void shouldFollowWaterSelection() {
        setUpJulyToSeptember();

        List<FootprintPoint> trend = evaluator.getFootprintTrend("water", Grouping.MONTHLY);

        assertEquals(5.0, kg(trend.getFirst()));
        assertEquals(2.0, kg(trend.get(2)));
    }

    @Test
    void shouldSumBothUtilitiesForBothSelection() {
        setUpJulyToSeptember();

        List<FootprintPoint> trend = evaluator.getFootprintTrend("both", Grouping.MONTHLY);

        assertEquals(13.0, kg(trend.getFirst()));    // 8 + 5
        assertEquals(4.0, kg(trend.get(2)));     // 2 + 2
    }

    //
    // Criteria: Periods with no readings are shown as "no data", clearly distinguished from a genuine zero footprint.
    //

    @Test
    void shouldMarkPeriodWithNoReadingsAsNoData() {
        setUpJulyToSeptember();

        FootprintPoint august = evaluator.getFootprintTrend("both", Grouping.MONTHLY).get(1);

        assertFalse(august.hasData());
        assertNull(august.kg());
    }

    @Test
    void shouldShowGenuineZeroWhenReadingsAreZero() {
        evaluator.setReadings(List.of(new Reading(LocalDateTime.of(2026, 8, 10, 12, 0), 0, "energy")));
        evaluator.setTimeRange(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 31));
        evaluator.setEmissionRate("energy", 0.5);

        FootprintPoint august = evaluator.getFootprintTrend("energy", Grouping.MONTHLY).getFirst();

        assertTrue(august.hasData());
        assertEquals(0.0, kg(august));
    }

    @Test
    void shouldBeNoDataWhenOnlyTheOtherUtilityHasReadings() {
        setUpJulyToSeptember();
        evaluator.setTimeRange(LocalDate.of(2026, 7, 9), LocalDate.of(2026, 7, 15));   // only the water reading

        FootprintPoint point = evaluator.getFootprintTrend("energy", Grouping.MONTHLY).getFirst();

        assertFalse(point.hasData());
    }

}