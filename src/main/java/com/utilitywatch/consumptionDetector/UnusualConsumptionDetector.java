package com.utilitywatch.consumptionDetector;

import java.util.Collections;
import java.util.List;
/**
 * A class handling simple unusual consumption detection for readings
 */
public class UnusualConsumptionDetector {
    /**
     * determines whether current consumption is more than 10%
     * above the maximum or below the minimum consumption from the previous week
     *
     * @param currentConsumption the current utility consumption
     * @param weeklyReadings the consumption readings used as the baseline
     * @return true if consumption is more than 10% above the weekly maximum
     * or more than 10% below the weekly minimum
     */
    public boolean isUnusual(double currentConsumption, List<Double> weeklyReadings) {

        // find the highest and lowest weekly readings
        double max = Collections.max(weeklyReadings);
        double min = Collections.min(weeklyReadings);

        // check if current consumption use is more than 10% above max or below min
        return currentConsumption > max * 1.10 || currentConsumption < min * 0.90;
    }
}
