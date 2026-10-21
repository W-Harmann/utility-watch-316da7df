package com.utilitywatch.reports;

import com.utilitywatch.CostBudgetEvaluator;
import com.utilitywatch.EnvironmentalEvaluator;
import com.utilitywatch.Reading;
import com.utilitywatch.conservationAdvisor.ConservationAdvisor;
import com.utilitywatch.consumptionDetector.ActiveHoursDetector;
import com.utilitywatch.consumptionDetector.LongDurationDetector;
import com.utilitywatch.consumptionDetector.SpikeDetector;
import com.utilitywatch.reports.CustomReport.Section;
import com.utilitywatch.reports.CustomReport.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static com.utilitywatch.ReadingEvaluator.BOTH;
import static com.utilitywatch.ReadingEvaluator.ENERGY;
import static com.utilitywatch.ReadingEvaluator.WATER;

/**
 * US7.3 / US11.2 - builds a consumption report containing only what the user selected:
 * the chosen utility (energy, water or both), the chosen date range, and the chosen sections.
 * Holds no JavaFX code so it can be unit tested; the Customise Report screen shows the result
 * and CustomReportWriter saves it to a file.
 */
public class CustomReportBuilder {

    /** Prices and emission factors used for the cost and environmental sections. */
    public record Rates(double energyCostPerKwh, double waterCostPerLitre,
                        double energyKgPerKwh, double waterKgPerLitre) {
    }

    // same detector settings as the Unusual Patterns tab on the Reports screen, so both agree
    private static final double SPIKE_FACTOR = 1.3;
    private static final double LONG_DURATION_BASELINE = 15.0;
    private static final int LONG_DURATION_HOURS = 72;
    private static final LocalTime ACTIVE_HOURS_START = LocalTime.of(6, 0);
    private static final LocalTime ACTIVE_HOURS_END = LocalTime.of(0, 0);
    private static final double OUTSIDE_HOURS_BASELINE = 1;

    /** Ranges up to this many days are grouped by week in the trend table; longer ones by month. */
    static final int WEEKLY_TREND_MAX_DAYS = 62;
    /** A change in average daily use smaller than this (in %) counts as steady. */
    static final double STEADY_TREND_PERCENT = 5.0;
    /** At most this many unusual patterns are listed per utility, to keep the report readable. */
    static final int MAX_UNUSUAL_ROWS = 15;

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final DateTimeFormatter DAY_TIME = DateTimeFormatter.ofPattern("d MMM yyyy HH:mm");
    private static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("MMM yyyy");

    private final ConsumptionSummaryCalculator calculator = new ConsumptionSummaryCalculator();
    private final ReportGenerator reportGenerator = new ReportGenerator();
    private final ConservationAdvisor conservationAdvisor = new ConservationAdvisor();

    /**
     * @param allReadings every reading the user has (they are filtered here)
     * @throws IllegalArgumentException if the options are not valid (see ReportOptions.validate)
     */
    public CustomReport build(List<Reading> allReadings, ReportOptions options, Rates rates) {
        return build(allReadings, options, rates, LocalDate.now());
    }

    CustomReport build(List<Reading> allReadings, ReportOptions options, Rates rates, LocalDate today) {
        Optional<String> problem = options.validate();
        if (problem.isPresent()) {
            throw new IllegalArgumentException(problem.get());
        }

        List<String> utilities = utilitiesFor(options.utility());
        boolean hasReadings = utilities.stream()
                .anyMatch(utility -> !readingsFor(allReadings, utility, options.from(), options.to()).isEmpty());

        List<Section> sections = new ArrayList<>();
        for (ReportSection type : options.sections()) { // EnumSet: always in report order
            sections.add(switch (type) {
                case CONSUMPTION_STATISTICS -> statistics(allReadings, options, utilities);
                case TRENDS_ANALYSIS -> trends(allReadings, options, utilities);
                case COMPARISONS -> comparisons(allReadings, options, utilities);
                case UNUSUAL_PATTERNS -> unusualPatterns(allReadings, options, utilities);
                case CONSERVATION_OPPORTUNITIES -> conservation(allReadings, options, utilities);
                case COST_SUMMARY -> costs(allReadings, options, utilities, rates);
                case ENVIRONMENTAL_IMPACT -> environmental(allReadings, options, utilities, rates);
                case READINGS_DATA -> readingsData(allReadings, options, utilities);
            });
        }
        return new CustomReport(utilityLabel(options.utility()), options.from(), options.to(), today,
                hasReadings, sections);
    }

    // ---- Consumption Statistics ----

    private Section statistics(List<Reading> all, ReportOptions options, List<String> utilities) {
        List<Table> tables = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        for (String utility : utilities) {
            List<Double> values = quantities(readingsFor(all, utility, options.from(), options.to()));
            if (values.isEmpty()) {
                notes.add("No " + name(utility).toLowerCase() + " readings in this period.");
                continue;
            }
            List<List<String>> rows = List.of(
                    List.of("Readings", String.valueOf(values.size())),
                    List.of("Total", amount(utility, calculator.calculateTotal(values))),
                    List.of("Average per reading", amount(utility, calculator.calculateAverage(values))),
                    List.of("Median", amount(utility, calculator.calculateMedian(values))),
                    List.of("Lowest reading", amount(utility, calculator.calculateMinimum(values))),
                    List.of("Highest reading", amount(utility, calculator.calculateMaximum(values))),
                    List.of("Standard deviation", amount(utility, calculator.calculateStandardDeviation(values))),
                    List.of("Variability", calculator.describeVariability(values)));
            tables.add(new Table(name(utility), List.of("Statistic", "Value"), rows));
        }
        return new Section(ReportSection.CONSUMPTION_STATISTICS, tables, notes);
    }

    // ---- Trends Analysis ----

    private Section trends(List<Reading> all, ReportOptions options, List<String> utilities) {
        boolean weekly = options.days() <= WEEKLY_TREND_MAX_DAYS;
        List<Table> tables = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        notes.add("Usage is grouped by " + (weekly ? "week" : "month")
                + "; partial periods at either end are compared by average use per day.");

        for (String utility : utilities) {
            List<Reading> readings = readingsFor(all, utility, options.from(), options.to());
            if (readings.isEmpty()) {
                notes.add("No " + name(utility).toLowerCase() + " readings in this period.");
                continue;
            }
            List<List<String>> rows = new ArrayList<>();
            Double firstPerDay = null;
            Double lastPerDay = null;
            int bucketsWithData = 0;

            for (LocalDate[] bucket : buckets(options.from(), options.to(), weekly)) {
                List<Double> values = quantities(readingsFor(readings, utility, bucket[0], bucket[1]));
                double total = calculator.calculateTotal(values);
                long days = ChronoUnit.DAYS.between(bucket[0], bucket[1]) + 1;
                double perDay = total / days;
                String label = weekly ? bucket[0].format(DAY) + " – " + bucket[1].format(DAY) : bucket[0].format(MONTH);
                rows.add(List.of(label, values.isEmpty() ? "No data" : amount(utility, total),
                        values.isEmpty() ? "–" : amount(utility, perDay), String.valueOf(values.size())));
                if (!values.isEmpty()) {
                    bucketsWithData++;
                    if (firstPerDay == null) {
                        firstPerDay = perDay;
                    }
                    lastPerDay = perDay;
                }
            }
            rows.add(List.of("Overall trend", describeTrend(bucketsWithData, firstPerDay, lastPerDay), "", ""));
            tables.add(new Table(name(utility), List.of(weekly ? "Week" : "Month", "Total", "Average per day", "Readings"), rows));
        }
        return new Section(ReportSection.TRENDS_ANALYSIS, tables, notes);
    }

    static String describeTrend(int bucketsWithData, Double first, Double last) {
        if (bucketsWithData < 2 || first == null || last == null || first == 0) {
            return "Not enough data to show a trend";
        }
        double change = (last - first) / first * 100;
        if (Math.abs(change) < STEADY_TREND_PERCENT) {
            return "Steady (" + signedPercent(change) + ")";
        }
        return (change > 0 ? "Increasing (" : "Decreasing (") + signedPercent(change) + ")";
    }

    /** Splits the range into 7-day weeks starting on the first day, or calendar months, cut to fit the range. */
    static List<LocalDate[]> buckets(LocalDate from, LocalDate to, boolean weekly) {
        List<LocalDate[]> buckets = new ArrayList<>();
        LocalDate start = from;
        while (!start.isAfter(to)) {
            LocalDate end = weekly ? start.plusDays(6) : YearMonth.from(start).atEndOfMonth();
            if (end.isAfter(to)) {
                end = to;
            }
            buckets.add(new LocalDate[]{start, end});
            start = end.plusDays(1);
        }
        return buckets;
    }

    // ---- Comparisons ----

    private Section comparisons(List<Reading> all, ReportOptions options, List<String> utilities) {
        LocalDate previousFrom = options.from().minusDays(options.days());
        LocalDate previousTo = options.from().minusDays(1);
        List<Table> tables = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        notes.add("Compared with the previous " + options.days() + " days ("
                + previousFrom.format(DAY) + " – " + previousTo.format(DAY) + ").");

        for (String utility : utilities) {
            List<Double> current = quantities(readingsFor(all, utility, options.from(), options.to()));
            List<Double> previous = quantities(readingsFor(all, utility, previousFrom, previousTo));
            if (current.isEmpty() || previous.isEmpty()) {
                notes.add("Not enough " + name(utility).toLowerCase() + " data to compare: "
                        + (current.isEmpty() ? "no readings in this period." : "no readings in the previous period."));
                continue;
            }
            double currentTotal = calculator.calculateTotal(current);
            double previousTotal = calculator.calculateTotal(previous);
            Optional<Double> averageChange = reportGenerator.compareToPreviousPeriod(Optional.of(previous), current);

            List<List<String>> rows = List.of(
                    List.of("Total usage", amount(utility, currentTotal), amount(utility, previousTotal),
                            percentChange(previousTotal, currentTotal)),
                    List.of("Average per reading", amount(utility, calculator.calculateAverage(current)),
                            amount(utility, calculator.calculateAverage(previous)),
                            averageChange.filter(Double::isFinite).map(CustomReportBuilder::signedPercent).orElse("–")),
                    List.of("Highest reading", amount(utility, calculator.calculateMaximum(current)),
                            amount(utility, calculator.calculateMaximum(previous)),
                            percentChange(calculator.calculateMaximum(previous), calculator.calculateMaximum(current))));
            tables.add(new Table(name(utility), List.of("Measure", "This period", "Previous period", "Change"), rows));
        }
        return new Section(ReportSection.COMPARISONS, tables, notes);
    }

    // ---- Unusual Patterns ----

    /** One flagged reading or run of readings. */
    record Flag(LocalDateTime start, LocalDateTime end, String reason, double usage) {
    }

    private Section unusualPatterns(List<Reading> all, ReportOptions options, List<String> utilities) {
        List<Table> tables = new ArrayList<>();
        List<String> notes = new ArrayList<>();
        for (String utility : utilities) {
            List<Reading> readings = readingsFor(all, utility, options.from(), options.to());
            if (readings.isEmpty()) {
                notes.add("No " + name(utility).toLowerCase() + " readings in this period.");
                continue;
            }
            List<Flag> flags = findUnusual(readings);
            if (flags.isEmpty()) {
                notes.add("No unusual " + name(utility).toLowerCase() + " patterns detected.");
                continue;
            }
            List<List<String>> rows = new ArrayList<>();
            for (Flag flag : flags.subList(0, Math.min(flags.size(), MAX_UNUSUAL_ROWS))) {
                String when = flag.start().equals(flag.end())
                        ? flag.start().format(DAY_TIME)
                        : flag.start().format(DAY_TIME) + " – " + flag.end().format(DAY_TIME);
                rows.add(List.of(when, flag.reason(), amount(utility, flag.usage())));
            }
            tables.add(new Table(name(utility) + " (" + flags.size() + " found)", List.of("When", "Pattern", "Usage"), rows));
            if (flags.size() > MAX_UNUSUAL_ROWS) {
                notes.add((flags.size() - MAX_UNUSUAL_ROWS) + " more " + name(utility).toLowerCase()
                        + " patterns not shown; see the Reports screen for the full list.");
            }
        }
        return new Section(ReportSection.UNUSUAL_PATTERNS, tables, notes);
    }

    /** Spikes, long periods of high use, and use outside active hours, oldest first. */
    List<Flag> findUnusual(List<Reading> sortedReadings) {
        List<Flag> flags = new ArrayList<>();

        for (Reading spike : new SpikeDetector(SPIKE_FACTOR).hasSpikes(sortedReadings)) {
            flags.add(new Flag(spike.getDateTime(), spike.getDateTime(), "Spike", spike.getQuantity()));
        }
        for (List<Reading> run : new LongDurationDetector(LONG_DURATION_BASELINE, LONG_DURATION_HOURS)
                .hasLongDurationUsage(sortedReadings)) {
            if (!run.isEmpty()) {
                flags.add(groupFlag(run, "Consistently high use"));
            }
        }
        ActiveHoursDetector activeHours = new ActiveHoursDetector(ACTIVE_HOURS_START, ACTIVE_HOURS_END);
        List<Reading> run = new ArrayList<>();
        for (Reading reading : sortedReadings) {
            if (activeHours.isUnusual(reading, OUTSIDE_HOURS_BASELINE)) {
                run.add(reading);
            } else if (!run.isEmpty()) {
                flags.add(groupFlag(run, "Use outside active hours"));
                run = new ArrayList<>();
            }
        }
        if (!run.isEmpty()) {
            flags.add(groupFlag(run, "Use outside active hours"));
        }

        flags.sort(Comparator.comparing(Flag::start));
        return flags;
    }

    private static Flag groupFlag(List<Reading> run, String reason) {
        double usage = run.stream().mapToDouble(Reading::getQuantity).sum();
        return new Flag(run.get(0).getDateTime(), run.get(run.size() - 1).getDateTime(), reason, usage);
    }

    // ---- Conservation Opportunities ----

    private Section conservation(List<Reading> all, ReportOptions options, List<String> utilities) {
        LocalDate previousFrom = options.from().minusDays(options.days());
        LocalDate previousTo = options.from().minusDays(1);
        List<Table> tables = new ArrayList<>();
        List<String> notes = new ArrayList<>();

        for (String utility : utilities) {
            List<Reading> current = readingsFor(all, utility, options.from(), options.to());
            if (current.isEmpty()) {
                notes.add("No " + name(utility).toLowerCase() + " readings in this period.");
                continue;
            }
            List<Reading> previous = readingsFor(all, utility, previousFrom, previousTo);
            List<List<String>> rows = new ArrayList<>();

            Optional<Double> averageChange = conservationAdvisor
                    .calculatePercentChange(Optional.of(previous), current).filter(Double::isFinite);
            rows.add(List.of("Average use vs previous period",
                    averageChange.map(CustomReportBuilder::signedPercent).orElse("Not enough data")));

            boolean previousPeakPositive = !previous.isEmpty()
                    && previous.stream().mapToDouble(Reading::getQuantity).max().orElse(0) > 0;
            Optional<Double> peakChange = previousPeakPositive
                    ? Optional.of(conservationAdvisor.compareConsumptionLevel(previous, current))
                    : Optional.empty();
            rows.add(List.of("Peak use vs previous period",
                    peakChange.map(CustomReportBuilder::signedPercent).orElse("Not enough data")));

            long outsideHours = findUnusual(current).stream()
                    .filter(flag -> flag.reason().equals("Use outside active hours")).count();
            rows.add(List.of("Periods of use outside active hours", String.valueOf(outsideHours)));

            for (String tip : tips(utility, averageChange.orElse(0.0), peakChange.orElse(0.0), outsideHours)) {
                rows.add(List.of("Suggestion", tip));
            }
            tables.add(new Table(name(utility), List.of("Insight", "Detail"), rows));
        }
        return new Section(ReportSection.CONSERVATION_OPPORTUNITIES, tables, notes);
    }

    static List<String> tips(String utility, double averageChange, double peakChange, long outsideHours) {
        List<String> tips = new ArrayList<>();
        boolean energy = ENERGY.equals(utility);
        if (averageChange > 0) {
            tips.add("Your average " + (energy ? "energy" : "water") + " use is up "
                    + String.format(Locale.ROOT, "%.1f%%", averageChange)
                    + " on the previous period. Look back at what changed to find the cause.");
        }
        if (peakChange > 0) {
            tips.add(energy
                    ? "Your peak use is higher than last period. Spread heavy appliances (dryer, dishwasher) across the day."
                    : "Your peak use is higher than last period. Check for leaks and long showers on high-use days.");
        }
        if (outsideHours > 0) {
            tips.add(energy
                    ? "Use overnight suggests devices left on standby. Switch them off at the wall."
                    : "Water use overnight can signal a leak. Check taps, toilets and irrigation timers.");
        }
        tips.add(energy
                ? "Turning your hot water system down by 5 degrees can save up to 10% on your energy bill."
                : "A 4-minute shower with a water-efficient head uses about half the water of an 8-minute one.");
        return tips;
    }

    // ---- Cost Summary ----

    private Section costs(List<Reading> all, ReportOptions options, List<String> utilities, Rates rates) {
        CostBudgetEvaluator evaluator = new CostBudgetEvaluator();
        evaluator.setReadings(all);
        evaluator.setTimeRange(options.from(), options.to());
        evaluator.setUtilityCost(ENERGY, rates.energyCostPerKwh());
        evaluator.setUtilityCost(WATER, rates.waterCostPerLitre());

        List<List<String>> rows = new ArrayList<>();
        for (String utility : utilities) {
            double usage = calculator.calculateTotal(quantities(readingsFor(all, utility, options.from(), options.to())));
            double rate = ENERGY.equals(utility) ? rates.energyCostPerKwh() : rates.waterCostPerLitre();
            rows.add(List.of(name(utility), amount(utility, usage),
                    "$" + plain(rate) + (ENERGY.equals(utility) ? "/kWh" : "/L"),
                    "$" + evaluator.calculateUtilityCostExact(utility).toPlainString()));
        }
        BigDecimal total = evaluator.calculateUtilityCostExact(options.utility());
        long months = ChronoUnit.MONTHS.between(YearMonth.from(options.from()), YearMonth.from(options.to())) + 1;
        BigDecimal perMonth = total.divide(BigDecimal.valueOf(months), 2, CostBudgetEvaluator.MONEY_ROUNDING);
        BigDecimal perDay = total.divide(BigDecimal.valueOf(options.days()), 2, CostBudgetEvaluator.MONEY_ROUNDING);

        rows.add(List.of("Total", "", "", "$" + total.toPlainString()));
        Table byUtility = new Table("Estimated cost", List.of("Utility", "Usage", "Rate", "Cost"), rows);
        Table averages = new Table("Averages", List.of("Measure", "Cost"), List.of(
                List.of("Average per day", "$" + perDay.toPlainString()),
                List.of("Average per month (" + months + (months == 1 ? " month)" : " months)"), "$" + perMonth.toPlainString())));
        return new Section(ReportSection.COST_SUMMARY, List.of(byUtility, averages),
                List.of("Costs are estimates based on the rates set on the Costs page."));
    }

    // ---- Environmental Impact ----

    private Section environmental(List<Reading> all, ReportOptions options, List<String> utilities, Rates rates) {
        EnvironmentalEvaluator evaluator = new EnvironmentalEvaluator();
        evaluator.setReadings(all);
        evaluator.setTimeRange(options.from(), options.to());
        evaluator.setEmissionRate(ENERGY, rates.energyKgPerKwh());
        evaluator.setEmissionRate(WATER, rates.waterKgPerLitre());

        List<List<String>> rows = new ArrayList<>();
        for (String utility : utilities) {
            double usage = calculator.calculateTotal(quantities(readingsFor(all, utility, options.from(), options.to())));
            double factor = ENERGY.equals(utility) ? rates.energyKgPerKwh() : rates.waterKgPerLitre();
            rows.add(List.of(name(utility), amount(utility, usage),
                    plain(factor) + (ENERGY.equals(utility) ? " kg/kWh" : " kg/L"),
                    kg(evaluator.calculateEmissionExact(utility))));
        }
        rows.add(List.of("Total", "", "", kg(evaluator.calculateEmissionExact(options.utility()))));
        Table footprint = new Table("Carbon footprint", List.of("Utility", "Usage", "Emission factor", "Footprint"), rows);

        List<List<String>> comparison = new ArrayList<>();
        if (evaluator.hasPreviousPeriodData(options.utility())) {
            comparison.add(List.of("Previous " + options.days() + " days",
                    kg(evaluator.calculatePreviousPeriodEmissionExact(options.utility()))));
            comparison.add(List.of("Change from previous period", signedPercent(evaluator.getPercentageChange(options.utility()))));
        } else {
            comparison.add(List.of("Change from previous period", "No previous data to compare"));
        }
        return new Section(ReportSection.ENVIRONMENTAL_IMPACT,
                List.of(footprint, new Table("Comparison", List.of("Measure", "Value"), comparison)),
                List.of("Emission factors are those set on the Impact page."));
    }

    // ---- Readings Data ----

    private Section readingsData(List<Reading> all, ReportOptions options, List<String> utilities) {
        List<Reading> readings = new ArrayList<>();
        for (String utility : utilities) {
            readings.addAll(readingsFor(all, utility, options.from(), options.to()));
        }
        readings.sort(Comparator.comparing(Reading::getDateTime));
        if (readings.isEmpty()) {
            return new Section(ReportSection.READINGS_DATA, List.of(), List.of("No readings in this period."));
        }
        List<List<String>> rows = new ArrayList<>();
        for (Reading reading : readings) {
            rows.add(List.of(reading.getDateTime().toLocalDate().toString(),
                    reading.getDateTime().toLocalTime().withNano(0).toString(),
                    name(reading.getResourceType()),
                    BigDecimal.valueOf(reading.getQuantity()).toPlainString(),
                    reading.getUnit()));
        }
        return new Section(ReportSection.READINGS_DATA,
                List.of(new Table(readings.size() + " readings", List.of("Date", "Time", "Utility", "Quantity", "Unit"), rows)),
                List.of());
    }

    // ---- helpers ----

    private static List<Reading> readingsFor(List<Reading> readings, String utility, LocalDate from, LocalDate to) {
        List<Reading> filtered = new ArrayList<>(ReadingFilter.byTypeAndPeriod(readings, utility, from, to));
        filtered.sort(Comparator.comparing(Reading::getDateTime));
        return filtered;
    }

    private static List<Double> quantities(List<Reading> readings) {
        return readings.stream().map(Reading::getQuantity).toList();
    }

    static List<String> utilitiesFor(String selection) {
        return BOTH.equals(selection) ? List.of(ENERGY, WATER) : List.of(selection);
    }

    static String utilityLabel(String selection) {
        return switch (selection) {
            case ENERGY -> "Energy";
            case WATER -> "Water";
            default -> "Energy and Water";
        };
    }

    private static String name(String utility) {
        return ENERGY.equals(utility) ? "Energy" : "Water";
    }

    static String amount(String utility, double value) {
        return ENERGY.equals(utility)
                ? String.format(Locale.ROOT, "%.2f kWh", value)
                : String.format(Locale.ROOT, "%.1f L", value);
    }

    private static String kg(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString() + " kg CO2e";
    }

    private static String percentChange(double previous, double current) {
        if (previous == 0) {
            return "–";
        }
        return signedPercent((current - previous) / previous * 100);
    }

    static String signedPercent(double value) {
        return String.format(Locale.ROOT, "%+.1f%%", value);
    }

    private static String plain(double value) {
        return BigDecimal.valueOf(value).stripTrailingZeros().toPlainString();
    }
}
