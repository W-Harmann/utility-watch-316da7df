package com.utilitywatch.goals;

import java.util.OptionalDouble;

// calculates goal progress and determines its current status
public class GoalProgressCalculator {

    // minimum target progress required to be on track (50%)
    public static final double ON_TRACK_THRESHOLD = 0.5;

    public enum Status { COMPLETED, ON_TRACK, BEHIND }

    /**
     * calculates percentage reduction from the previous total
     * returns empty if the previous total is zero or negative
     * consumption increases count as 0% reduction
     */
    public OptionalDouble reductionPercent(double previousTotal, double currentTotal) {
        if (previousTotal <= 0) {
            return OptionalDouble.empty();
        }
        double reduction = (previousTotal - currentTotal) / previousTotal * 100;
        return OptionalDouble.of(Math.max(0.0, reduction));
    }

    // calculates target progress as a fraction between 0 and 1
    public double progressFraction(double achievedReduction, double targetReduction) {
        if (targetReduction <= 0) {
            return 1.0;
        }
        return Math.clamp(achievedReduction / targetReduction, 0.0, 1.0);
    }

    // returns whether the goal is completed, on track (at least 50%), or behind
    public Status status(double achievedReduction, double targetReduction) {
        double fraction = progressFraction(achievedReduction, targetReduction);
        if (fraction >= 1.0) {
            return Status.COMPLETED;
        }
        if (fraction >= ON_TRACK_THRESHOLD) {
            return Status.ON_TRACK;
        }
        return Status.BEHIND;
    }
}
