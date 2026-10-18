package com.utilitywatch.home;

import com.utilitywatch.CostBudgetEvaluator;
import com.utilitywatch.Reading;
import com.utilitywatch.ReadingEvaluator;
import com.utilitywatch.reports.ReportsDataService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * builds the numbers shown on the home dashboard using the user's actual readings
 */
public class DashboardSummaryService {

    private static final int TREND_DAYS = 7;
    private final ReportsDataService dataService;

    // creates a service that loads the user's readings
    public DashboardSummaryService() {
        this(new ReportsDataService());
    }

    // creates a service using the given data source
    public DashboardSummaryService(ReportsDataService dataService) {
        this.dataService = dataService;
    }

    /**
     * builds a summary for the most recent day with readings
     *
     * @return the summary, or empty if there are no readings
     */
    public Optional<DashboardSummary> buildSummary() {
        Optional<LocalDate> latestReadingDay = findLatestReadingDay();
        if (latestReadingDay.isEmpty()) {
            return Optional.empty();
        }

        LocalDate summaryDay = latestReadingDay.get();
        List<LocalDate> trendDays = new ArrayList<>();
        List<Double> energyTrend = new ArrayList<>();
        List<Double> waterTrend = new ArrayList<>();

        // collects the last 7 days of readings, starting with the oldest day
        for (int daysAgo = TREND_DAYS - 1; daysAgo >= 0; daysAgo--) {
            LocalDate trendDay = summaryDay.minusDays(daysAgo);
            trendDays.add(trendDay);
            energyTrend.add(dailyTotal(ReadingEvaluator.ENERGY, trendDay));
            waterTrend.add(dailyTotal(ReadingEvaluator.WATER, trendDay));
        }

        return Optional.of(new DashboardSummary(summaryDay,
            dailyTotal(ReadingEvaluator.ENERGY, summaryDay),
            dailyTotal(ReadingEvaluator.WATER, summaryDay),
            trendDays, energyTrend, waterTrend));
    }

    /**
     * estimates the cost of one day's energy and water usage
     *
     * @param day the day to calculate the cost for
     * @param energyRate cost per kWh
     * @param waterRate cost per litre
     * @return the estimated cost in dollars
     */
    public double estimateDailyCost(LocalDate day, double energyRate, double waterRate) {
        List<Reading> dailyReadings = new ArrayList<>();
        dailyReadings.addAll(dataService.getReadings(ReadingEvaluator.ENERGY, day, day));
        dailyReadings.addAll(dataService.getReadings(ReadingEvaluator.WATER, day, day));

        // sets up the calculator with the readings and the selected day's date range
        CostBudgetEvaluator costEvaluator = new CostBudgetEvaluator();
        costEvaluator.setReadings(dailyReadings);
        costEvaluator.setTimeRange(day, day);

        // sets the cost per unit for energy and water
        costEvaluator.setUtilityCost(ReadingEvaluator.ENERGY, energyRate);
        costEvaluator.setUtilityCost(ReadingEvaluator.WATER, waterRate);

        // calculates and returns the combined cost of energy and water usage
        return costEvaluator.calculateUtilityCost(ReadingEvaluator.BOTH);
    }

    // calculates the total quantity used for one resource on a given day
    private double dailyTotal(String resourceType, LocalDate day) {
        return dataService.getReadings(resourceType, day, day).stream().mapToDouble(Reading::getQuantity).sum();
    }

    // finds the latest date with an energy or water reading
    private Optional<LocalDate> findLatestReadingDay() {
        LocalDate earliestDate = LocalDate.of(1900, 1, 1);
        LocalDate today = LocalDate.now();

        // gets all energy and water readings recorded between the earliest date and today
        return Stream.of(ReadingEvaluator.ENERGY, ReadingEvaluator.WATER)
                .flatMap(resourceType -> dataService.getReadings(resourceType, earliestDate, today).stream())
                .map(reading -> reading.getDateTime().toLocalDate())
                .max(Comparator.naturalOrder());
    }
}