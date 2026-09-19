package com.utilitywatch;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * US10.1 - Estimates CO2-equivalent emissions (kg) from utility usage.
 */
public class EnvironmentalEvaluator extends ReadingEvaluator {

    private final Map<String, BigDecimal> emissionRates = new HashMap<>();

    /** Sets kg of CO2 per unit of usage. Negative rates are rejected. */
    public boolean setEmissionRate(String utility, double kgPerUnit) {
        requireKnownUtility(utility);
        if (kgPerUnit < 0) {
            return false;
        }
        emissionRates.put(utility, BigDecimal.valueOf(kgPerUnit));
        return true;
    }

    public double getEmissionRate(String utility) {
        requireKnownUtility(utility);
        return emissionRates.getOrDefault(utility, BigDecimal.ZERO).doubleValue();
    }

    /** Exact kg of CO2 for "power", "water" or "both" in the time range. */
    public BigDecimal calculateEmissionExact(String selection) {
        BigDecimal total = BigDecimal.ZERO;
        for (String utility : resolveSelection(selection)) {
            BigDecimal rate = emissionRates.getOrDefault(utility, BigDecimal.ZERO);
            total = total.add(totalUsage(utility).multiply(rate));
        }
        return total;
    }

    /** Name kept to match the existing tests; "calculateEmission" would be clearer. */
    public double calculateEmissionPower(String selection) {
        return calculateEmissionExact(selection).doubleValue();
    }
}
