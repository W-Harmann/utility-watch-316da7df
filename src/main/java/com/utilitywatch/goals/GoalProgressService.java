package com.utilitywatch.goals;


import com.utilitywatch.reports.ConsumptionSummaryCalculator;
import com.utilitywatch.reports.ReportsDataService;

import java.time.LocalDate;
import java.util.List;
import java.util.OptionalDouble;

/**
 * calculates how much the user's resource consumption has decreased towards a goal
 */
public class GoalProgressService {

    private final ReportsDataService dataService = new ReportsDataService();
    private final ConsumptionSummaryCalculator calculator = new ConsumptionSummaryCalculator();
    private final GoalProgressCalculator progressCalculator = new GoalProgressCalculator();

    /**
     * compares resource consumption in the current goal period with the previous period
     *
     * @param goal the goal to check
     * @return the percentage reduction, or empty if either period has no readings
     */
    public OptionalDouble achievedReduction(Goal goal) {
        // get the number of days in the goal period
        int days = periodToDays(goal.getTargetPeriod());
        LocalDate today = LocalDate.now();
        LocalDate currentStart = today.minusDays(days - 1);
        LocalDate previousEnd = currentStart.minusDays(1);
        LocalDate previousStart = previousEnd.minusDays(days - 1);

        List<Double> current = dataService.getReadingQuantities(goal.getResourceType(), currentStart, today);
        List<Double> previous = dataService.getReadingQuantities(goal.getResourceType(), previousStart, previousEnd);

        // skip calculation if either period has no readings
        if (current.isEmpty() || previous.isEmpty()) {
            return OptionalDouble.empty();
        }

        // calculate the percentage reduction between the two period totals
        return progressCalculator.reductionPercent(calculator.calculateTotal(previous), calculator.calculateTotal(current));
    }

    private int periodToDays(String period) {
        return switch (period) {
            case "1 Month" -> 30;
            case "3 Months" -> 90;
            case "6 Months" -> 180;
            case "1 Year" -> 365;
            default -> 30;
        };
    }
}
