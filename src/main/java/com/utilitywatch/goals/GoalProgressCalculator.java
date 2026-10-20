package com.utilitywatch.goals;

import java.util.OptionalDouble;

/**
 * Calculates how far a goal has progress and works out its current status (us8.3).
 * Has no JavaFx so it can be unit tested.
 */
public class GoalProgressCalculator {

    /** minimum target progress required to be on track (50%) */
    public static final double ON_TRACK_THRESHOLD = 0.5;

    /** how a goal is currently tracking against its target */
    public enum Status {
        /** the target reduction has been reached */
        COMPLETED,
        /** at least half of the target has been reached */
        ON_TRACK,
        /** less than half of the target has been reached */
        BEHIND
    }

    /**
     * calculates percentage reduction from the previous total
     * returns empty if the previous total is zero or negative
     * consumption increases count as 0% reduction
     * @param previousTotal total consumption in the earlier period
     * @param currentTotal total consumption in the latest period
     * @return the percentage reduction, or empty if the previous total is zero or negative
     */
    public OptionalDouble reductionPercent(double previousTotal, double currentTotal) {
        if (previousTotal <= 0) {
            return OptionalDouble.empty();
        }
        double reduction = (previousTotal - currentTotal) / previousTotal * 100;
        return OptionalDouble.of(Math.max(0.0, reduction));
    }

    // calculates target progress as a fraction between 0 and 1

    /**
     * calculates progress towards the target as a fraction between 0 and 1.
     * a target of 0 or less counts as fully achieved.
     * @param achievedReduction the percentage reduction achieved so far
     * @param targetReduction the percentage reduction the user is aiming for
     * @return the progress fraction, up to 1
     */
    public double progressFraction(double achievedReduction, double targetReduction) {
        if (targetReduction <= 0) {
            return 1.0;
        }
        return Math.clamp(achievedReduction / targetReduction, 0.0, 1.0);
    }

    /**
     * returns whether the goal is completed, on track (at least 50%), or behind
     * @param achievedReduction the percentage reduction achieved so far
     * @param targetReduction the percentage reduction the user is aiming for
     * @return the goal's status
     */
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
