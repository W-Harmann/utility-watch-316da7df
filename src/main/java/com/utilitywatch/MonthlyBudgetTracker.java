package com.utilitywatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

import static com.utilitywatch.CostBudgetEvaluator.MONEY_ROUNDING;
import static com.utilitywatch.ReadingEvaluator.BOTH;
import static com.utilitywatch.ReadingEvaluator.ENERGY;
import static com.utilitywatch.ReadingEvaluator.WATER;

/**
 * US9.3 - tracks spending (energy + water) against a monthly budget for the current calendar month,
 * and projects the month-end total so the user can be warned while there is still time to adjust.
 *
 * The projection is a straight-line estimate: spent so far / days elapsed x days in the month.
 * Days elapsed counts today, so it is 1 on the first of the month and never 0.
 */
public class MonthlyBudgetTracker {

    public enum Status {
        /** No budget set: nothing to warn about. */
        NO_BUDGET,
        /** A budget is set but there are no readings yet this month: nothing to warn about. */
        NO_READINGS,
        /** Spending and the projection are both within the budget. */
        ON_TRACK,
        /** Still within budget, but the projection says it will be exceeded by month end. */
        AT_RISK,
        /** Actual spending is already more than the budget. */
        OVER_BUDGET
    }

    private final CostBudgetEvaluator evaluator = new CostBudgetEvaluator();
    private final YearMonth month;
    private final LocalDate today;
    private final BigDecimal budget;
    private final boolean hasReadingsThisMonth;

    /**
     * @param readings    all of the user's readings (only this month's are counted)
     * @param energyRate  $ per kWh
     * @param waterRate   $ per litre
     * @param budget      monthly budget in $, 0 (or less) for none
     * @param today       the current date, which decides the month and the days elapsed
     */
    public MonthlyBudgetTracker(List<Reading> readings, double energyRate, double waterRate,
                                double budget, LocalDate today) {
        this.today = today;
        this.month = YearMonth.from(today);
        this.budget = budget > 0 ? BigDecimal.valueOf(budget) : BigDecimal.ZERO;

        LocalDateTime start = month.atDay(1).atStartOfDay();
        LocalDateTime end = today.atTime(LocalTime.MAX);
        evaluator.setReadings(readings);
        evaluator.setTimeRange(start, end);
        evaluator.setUtilityCost(ENERGY, energyRate);
        evaluator.setUtilityCost(WATER, waterRate);
        evaluator.setMonthlyBudget(this.budget.doubleValue());

        hasReadingsThisMonth = readings.stream().anyMatch(reading -> reading.getDateTime() != null
                && !reading.getDateTime().isBefore(start) && !reading.getDateTime().isAfter(end));
    }

    public YearMonth getMonth() {
        return month;
    }

    public boolean hasBudget() {
        return budget.signum() > 0;
    }

    public boolean hasReadingsThisMonth() {
        return hasReadingsThisMonth;
    }

    /** $ spent from the 1st of the month up to the end of today. */
    public BigDecimal getSpentSoFar() {
        return evaluator.calculateUtilityCostExact(BOTH);
    }

    public BigDecimal getBudget() {
        return budget.setScale(2, MONEY_ROUNDING);
    }

    /** % of the budget spent so far (can be over 100). 0 when no budget is set. */
    public double getPercentUsed() {
        return evaluator.getBudgetUsedPercentage();
    }

    /** Budget left; never negative. See getAmountOver for how far over the budget spending is. */
    public BigDecimal getRemaining() {
        return budget.subtract(getSpentSoFar()).max(BigDecimal.ZERO).setScale(2, MONEY_ROUNDING);
    }

    /** How far spending is over the budget; 0 when it isn't. */
    public BigDecimal getAmountOver() {
        if (!hasBudget()) {
            return BigDecimal.ZERO.setScale(2, MONEY_ROUNDING);
        }
        return getSpentSoFar().subtract(budget).max(BigDecimal.ZERO).setScale(2, MONEY_ROUNDING);
    }

    /** Days of the month so far, counting today: 1 on the 1st, so the projection never divides by zero. */
    public int getDaysElapsed() {
        return today.getDayOfMonth();
    }

    public int getDaysInMonth() {
        return month.lengthOfMonth();
    }

    /** Days left after today. */
    public int getDaysRemaining() {
        return getDaysInMonth() - getDaysElapsed();
    }

    /** Estimated total for the whole month if spending continues at the same daily rate. */
    public BigDecimal getProjectedMonthTotal() {
        return getSpentSoFar()
                .multiply(BigDecimal.valueOf(getDaysInMonth()))
                .divide(BigDecimal.valueOf(getDaysElapsed()), 2, MONEY_ROUNDING);
    }

    /** % of the budget the projection would use. 0 when no budget is set. */
    public double getProjectedPercent() {
        if (!hasBudget()) {
            return 0;
        }
        return getProjectedMonthTotal().multiply(BigDecimal.valueOf(100))
                .divide(budget, 2, RoundingMode.HALF_UP).doubleValue();
    }

    /** Average $ per day the user can still spend for the rest of the month and stay within budget. */
    public BigDecimal getDailyAllowance() {
        if (getDaysRemaining() <= 0) {
            return BigDecimal.ZERO.setScale(2, MONEY_ROUNDING);
        }
        return getRemaining().divide(BigDecimal.valueOf(getDaysRemaining()), 2, RoundingMode.DOWN);
    }

    public Status getStatus() {
        if (!hasBudget()) {
            return Status.NO_BUDGET;
        }
        if (!hasReadingsThisMonth) {
            return Status.NO_READINGS;
        }
        if (evaluator.isOverBudget()) {
            return Status.OVER_BUDGET;
        }
        if (getProjectedMonthTotal().compareTo(budget) > 0) {
            return Status.AT_RISK;
        }
        return Status.ON_TRACK;
    }

    /** True when the user should see a warning: on track to exceed, or already over. */
    public boolean shouldWarn() {
        Status status = getStatus();
        return status == Status.AT_RISK || status == Status.OVER_BUDGET;
    }
}
