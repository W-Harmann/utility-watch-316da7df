package com.utilitywatch;

import com.utilitywatch.EnvironmentalEvaluator.FootprintPoint;
import com.utilitywatch.EnvironmentalEvaluator.Grouping;

import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * US10.3 - Turns the footprint trend (EnvironmentalEvaluator.getFootprintTrend) into the text and
 * line segments the Impact page chart needs. Kept free of JavaFX so it can be unit tested.
 */
public final class FootprintTrendFormatter {

    // English month names so labels read the same on every machine (en_AU would give "Sept")
    private static final DateTimeFormatter MONTH_YEAR = DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH);
    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH);
    private static final DateTimeFormatter DAY_YEAR = DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.ENGLISH);

    private FootprintTrendFormatter() {
    }

    /**
     * One x-axis label per point, in the same order. Months are "Jul 2026"; weeks are their first day ("Jul 6"),
     * with the year added when the points cover more than one year so a label is never repeated.
     */
    public static List<String> labels(List<FootprintPoint> points, Grouping grouping) {
        boolean severalYears = points.stream().map(point -> point.periodStart().getYear()).distinct().count() > 1;
        DateTimeFormatter format = grouping == Grouping.MONTHLY ? MONTH_YEAR : severalYears ? DAY_YEAR : DAY;

        List<String> labels = new ArrayList<>();
        for (FootprintPoint point : points) {
            labels.add(point.periodStart().format(format));
        }
        return labels;
    }

    /** Tooltip text, e.g. "Jul 6 – Jul 12, 2026: 12.35 kg CO₂e" or "Aug 1 – Aug 31, 2026: no data (no readings)". */
    public static String describe(FootprintPoint point) {
        String value = point.hasData()
                ? point.kg().setScale(2, RoundingMode.HALF_UP).toPlainString() + " kg CO₂e"
                : "no data (no readings)";
        return dates(point.periodStart(), point.periodEnd()) + ": " + value;
    }

    /**
     * Index lists of consecutive points that have data. The chart draws each run as its own line,
     * so the line stops at a period with no data instead of joining across it.
     */
    public static List<List<Integer>> runsWithData(List<FootprintPoint> points) {
        List<List<Integer>> runs = new ArrayList<>();
        List<Integer> current = new ArrayList<>();
        for (int i = 0; i < points.size(); i++) {
            if (points.get(i).hasData()) {
                current.add(i);
            } else if (!current.isEmpty()) {
                runs.add(current);
                current = new ArrayList<>();
            }
        }
        if (!current.isEmpty()) {
            runs.add(current);
        }
        return runs;
    }

    /** e.g. "2 of 13 weeks have no readings"; empty when every period has readings. */
    public static String noDataSummary(List<FootprintPoint> points, Grouping grouping) {
        long noData = points.stream().filter(point -> !point.hasData()).count();
        if (noData == 0) {
            return "";
        }
        if (noData == points.size()) {
            return "No readings in the selected period";
        }
        String unit = grouping == Grouping.MONTHLY ? "month" : "week";
        return noData == 1
                ? "1 of " + points.size() + " " + unit + "s has no readings"
                : noData + " of " + points.size() + " " + unit + "s have no readings";
    }

    private static String dates(LocalDate start, LocalDate end) {
        if (start.equals(end)) {
            return start.format(DAY_YEAR);
        }
        if (start.getYear() == end.getYear()) {
            return start.format(DAY) + " – " + end.format(DAY_YEAR);
        }
        return start.format(DAY_YEAR) + " – " + end.format(DAY_YEAR);
    }
}
