package com.utilitywatch;

import java.util.Collections;
import java.util.List;

public class UnusualConsumptionDetector {
    public boolean isUnusual(double currentConsumption, List<Double> weeklyReadings) {
        /**
         * determines whether current consumption is more than 10%
         * above the maximum consumption from the previous week
         * @param currentConsumption the current utility consumption
         * @param weeklyReadings the consumption readings used as the baseline
         * @return true if consumption is more than 10% above the weekly maximum
         */
        double max = Collections.max(weeklyReadings);
        return currentConsumption > max * 1.10;
    }
}
