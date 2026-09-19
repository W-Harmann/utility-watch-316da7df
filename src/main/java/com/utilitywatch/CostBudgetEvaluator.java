package com.utilitywatch;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

/**
 * US9.1 - Tracks utility usage in dollars.
 * All money is held as BigDecimal internally to avoid floating-point rounding error.
 * The double getters exist because the tests compare against double literals.
 */
public class CostBudgetEvaluator extends ReadingEvaluator {

    /** Standard money rounding: 0.005 and above rounds up. */
    public static final RoundingMode MONEY_ROUNDING = RoundingMode.HALF_UP;

    private final Map<String, BigDecimal> rates = new HashMap<>();
    private BigDecimal monthlyBudget = BigDecimal.ZERO;

    /** @return true if stored, false if rejected (negative). The old value is kept on rejection. */
    public boolean setMonthlyBudget(double budget) {
        if (budget < 0) {
            return false;
        }
        monthlyBudget = BigDecimal.valueOf(budget);
        return true;
    }

    public double getMonthlyBudget() {
        return monthlyBudget.doubleValue();
    }

    /** Sets the dollar cost per unit (kWh for power, kL for water). Negative rates are rejected. */
    public boolean setUtilityCost(String utility, double costPerUnit) {
        requireKnownUtility(utility);
        if (costPerUnit < 0) {
            return false;
        }
        rates.put(utility, BigDecimal.valueOf(costPerUnit));
        return true;
    }

    /** Returns the rate for a utility, or 0 if none has been set. */
    public double getUtilityCost(String utility) {
        requireKnownUtility(utility);
        return rates.getOrDefault(utility, BigDecimal.ZERO).doubleValue();
    }

    /** Exact cost for "power", "water" or "both" in the time range, always at 2 decimal places. */
    public BigDecimal calculateUtilityCostExact(String selection) {
        BigDecimal total = BigDecimal.ZERO;
        for (String utility : resolveSelection(selection)) {
            BigDecimal rate = rates.getOrDefault(utility, BigDecimal.ZERO);
            total = total.add(totalUsage(utility).multiply(rate));
        }
        return total.setScale(2, MONEY_ROUNDING);
    }

    public double calculateUtilityCost(String selection) {
        return calculateUtilityCostExact(selection).doubleValue();
    }

    /** For display in the UI: always two decimal places, e.g. "$6.00". */
    public String formatUtilityCost(String selection) {
        return "$" + calculateUtilityCostExact(selection).toPlainString();
    }
}
