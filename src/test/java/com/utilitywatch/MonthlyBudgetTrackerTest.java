package com.utilitywatch;

// US9.3 - As a Household user, I want to set a monthly spending budget and be warned when my estimated cost
// is on track to exceed it so that I can adjust my usage in time.
//      [x] For the current calendar month, the app shows the amount spent so far, the budget, the percentage used,
//          and the amount remaining.
//      [x] A separate "over budget" state is shown once actual spending exceeds the budget.
//      [x] No warning is shown when no budget is set or there are no readings this month, and the projection works
//          on the first day of the month without dividing by zero.

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MonthlyBudgetTrackerTest {

    // $1 per kWh and $0.01 per litre keep the arithmetic easy to follow
    private static final double ENERGY_RATE = 1.0;
    private static final double WATER_RATE = 0.01;

    private static Reading energy(int year, int month, int day, double kwh) {
        return new Reading(LocalDateTime.of(year, month, day, 12, 0), kwh, "energy");
    }

    private static Reading water(int year, int month, int day, double litres) {
        return new Reading(LocalDateTime.of(year, month, day, 12, 0), litres, "water");
    }

    private static MonthlyBudgetTracker tracker(List<Reading> readings, double budget, LocalDate today) {
        return new MonthlyBudgetTracker(readings, ENERGY_RATE, WATER_RATE, budget, today);
    }

    // ---- spent so far, budget, % used, remaining ----

    @Test
    void showsSpentBudgetPercentAndRemainingForCurrentMonth() {
        // 30 kWh ($30) + 1000 L ($10) = $40 of a $100 budget
        List<Reading> readings = List.of(energy(2026, 10, 3, 30), water(2026, 10, 5, 1000));
        MonthlyBudgetTracker tracker = tracker(readings, 100, LocalDate.of(2026, 10, 10));

        assertEquals(new BigDecimal("40.00"), tracker.getSpentSoFar());
        assertEquals(new BigDecimal("100.00"), tracker.getBudget());
        assertEquals(40.0, tracker.getPercentUsed(), 0.001);
        assertEquals(new BigDecimal("60.00"), tracker.getRemaining());
    }

    @Test
    void onlyCountsReadingsFromTheCurrentCalendarMonth() {
        List<Reading> readings = List.of(
                energy(2026, 9, 30, 500),   // last month - ignored
                energy(2026, 10, 1, 10),
                energy(2025, 10, 15, 500)); // same month last year - ignored
        MonthlyBudgetTracker tracker = tracker(readings, 100, LocalDate.of(2026, 10, 15));

        assertEquals(new BigDecimal("10.00"), tracker.getSpentSoFar());
    }

    // ---- projection and warning ----

    @Test
    void projectsMonthEndFromDailyRate() {
        // $40 after 10 days of a 31-day month -> $4/day -> $124
        List<Reading> readings = List.of(energy(2026, 10, 2, 40));
        MonthlyBudgetTracker tracker = tracker(readings, 200, LocalDate.of(2026, 10, 10));

        assertEquals(10, tracker.getDaysElapsed());
        assertEquals(31, tracker.getDaysInMonth());
        assertEquals(new BigDecimal("124.00"), tracker.getProjectedMonthTotal());
        assertEquals(62.0, tracker.getProjectedPercent(), 0.001);
    }

    @Test
    void onTrackWhenProjectionIsWithinBudget() {
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 10, 2, 40)), 200, LocalDate.of(2026, 10, 10));

        assertEquals(MonthlyBudgetTracker.Status.ON_TRACK, tracker.getStatus());
        assertFalse(tracker.shouldWarn());
    }

    @Test
    void warnsWhenOnTrackToExceedBudgetBeforeActuallyExceedingIt() {
        // $40 spent of $100 so far, but projected $124 by month end
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 10, 2, 40)), 100, LocalDate.of(2026, 10, 10));

        assertEquals(MonthlyBudgetTracker.Status.AT_RISK, tracker.getStatus());
        assertTrue(tracker.shouldWarn());
        // $60 left over 21 days -> keep under $2.85/day (rounded down so it really stays within budget)
        assertEquals(21, tracker.getDaysRemaining());
        assertEquals(new BigDecimal("2.85"), tracker.getDailyAllowance());
    }

    @Test
    void projectionExactlyOnBudgetIsNotAWarning() {
        // $10 after 10 days of a 30-day month projects exactly $30
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 9, 1, 10)), 30, LocalDate.of(2026, 9, 10));

        assertEquals(new BigDecimal("30.00"), tracker.getProjectedMonthTotal());
        assertEquals(MonthlyBudgetTracker.Status.ON_TRACK, tracker.getStatus());
    }

    // ---- separate over budget state ----

    @Test
    void separateOverBudgetStateOnceSpendingExceedsBudget() {
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 10, 2, 120)), 100, LocalDate.of(2026, 10, 10));

        assertEquals(MonthlyBudgetTracker.Status.OVER_BUDGET, tracker.getStatus());
        assertTrue(tracker.shouldWarn());
        assertEquals(120.0, tracker.getPercentUsed(), 0.001);
        assertEquals(new BigDecimal("0.00"), tracker.getRemaining()); // never negative
        assertEquals(new BigDecimal("20.00"), tracker.getAmountOver());
    }

    @Test
    void spendingExactlyEqualToBudgetIsNotOverBudget() {
        // spent == budget on the last day: not over, and the projection equals the budget
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 10, 5, 100)), 100, LocalDate.of(2026, 10, 31));

        assertNotEquals(MonthlyBudgetTracker.Status.OVER_BUDGET, tracker.getStatus());
        assertEquals(new BigDecimal("0.00"), tracker.getAmountOver());
        assertEquals(new BigDecimal("0.00"), tracker.getDailyAllowance()); // no days left, no division by zero
    }

    // ---- no warning cases ----

    @Test
    void noWarningWhenNoBudgetIsSet() {
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 10, 2, 999)), 0, LocalDate.of(2026, 10, 10));

        assertFalse(tracker.hasBudget());
        assertEquals(MonthlyBudgetTracker.Status.NO_BUDGET, tracker.getStatus());
        assertFalse(tracker.shouldWarn());
        assertEquals(0.0, tracker.getPercentUsed(), 0.001);
        assertEquals(0.0, tracker.getProjectedPercent(), 0.001);
    }

    @Test
    void noWarningWhenThereAreNoReadingsThisMonth() {
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 9, 20, 999)), 100, LocalDate.of(2026, 10, 10));

        assertFalse(tracker.hasReadingsThisMonth());
        assertEquals(MonthlyBudgetTracker.Status.NO_READINGS, tracker.getStatus());
        assertFalse(tracker.shouldWarn());
        assertEquals(new BigDecimal("0.00"), tracker.getSpentSoFar());
    }

    @Test
    void noWarningWhenUserHasNoReadingsAtAll() {
        MonthlyBudgetTracker tracker = tracker(List.of(), 100, LocalDate.of(2026, 10, 10));

        assertEquals(MonthlyBudgetTracker.Status.NO_READINGS, tracker.getStatus());
        assertFalse(tracker.shouldWarn());
    }

    @Test
    void projectionWorksOnFirstDayOfMonthWithoutDividingByZero() {
        // $5 on the 1st of a 31-day month -> projected $155
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 10, 1, 5)), 100, LocalDate.of(2026, 10, 1));

        assertEquals(1, tracker.getDaysElapsed());
        assertEquals(new BigDecimal("155.00"), assertDoesNotThrow(tracker::getProjectedMonthTotal));
        assertEquals(MonthlyBudgetTracker.Status.AT_RISK, tracker.getStatus());
    }

    @Test
    void firstDayOfMonthWithNoReadingsProjectsZero() {
        MonthlyBudgetTracker tracker = tracker(List.of(), 100, LocalDate.of(2026, 10, 1));

        assertEquals(new BigDecimal("0.00"), assertDoesNotThrow(tracker::getProjectedMonthTotal));
        assertFalse(tracker.shouldWarn());
    }

    @Test
    void handlesLeapYearFebruary() {
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2028, 2, 1, 29)), 100, LocalDate.of(2028, 2, 29));

        assertEquals(29, tracker.getDaysInMonth());
        assertEquals(0, tracker.getDaysRemaining());
        assertEquals(new BigDecimal("29.00"), tracker.getProjectedMonthTotal());
    }

    @Test
    void negativeBudgetIsTreatedAsNoBudget() {
        MonthlyBudgetTracker tracker = tracker(List.of(energy(2026, 10, 2, 10)), -50, LocalDate.of(2026, 10, 10));

        assertEquals(MonthlyBudgetTracker.Status.NO_BUDGET, tracker.getStatus());
    }
}
