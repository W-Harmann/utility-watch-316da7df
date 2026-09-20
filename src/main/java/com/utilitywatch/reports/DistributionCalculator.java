package com.utilitywatch.reports;

import java.util.ArrayList;
import java.util.List;

// calculates how readings are grouped into value ranges
public class DistributionCalculator {

    public static class Range {
        public final double start;
        public final double end;
        public final int count;

        public Range(double start, double end, int count) {
            this.start = start;
            this.end = end;
            this.count = count;
        }
    }

    private static final int RANGE_COUNT = 6;

    // groups readings into RANGE_COUNT ranges between the min and max
    public List<Range> computeRanges(List<Double> readings, double min, double max) {
        // ensure the range is at least 1 to prevent division by zero when all readings have the same value
        double range = Math.max(max - min, 1);
        double rangeSize = range / RANGE_COUNT;

        // stores how many readings belong to each range
        int[] counts = new int[RANGE_COUNT];

        // assign each reading to the appropriate range
        for (double reading : readings) {
            int rangeIndex = (int) ((reading - min) / rangeSize);

            // keeps the maximum value inside the last range
            if (rangeIndex >= RANGE_COUNT) {
                rangeIndex = RANGE_COUNT - 1;
            }
            counts[rangeIndex]++;
        }

        List<Range> ranges = new ArrayList<>();

        // creates each range with its start, end, and count
        for (int rangeNumber = 0; rangeNumber < RANGE_COUNT; rangeNumber++) {
            double start = min + rangeNumber * rangeSize;
            ranges.add(new Range(start, start + rangeSize, counts[rangeNumber]));
        }
        return ranges;
    }

    // fings which range contains the most readings
    public Range findMostCommonRange(List<Range> ranges) {
        Range mostCommonRange = ranges.get(0);

        for (Range range : ranges) {
            if (range.count > mostCommonRange.count) {
                mostCommonRange = range;
            }
        }
        return mostCommonRange;
    }
}
