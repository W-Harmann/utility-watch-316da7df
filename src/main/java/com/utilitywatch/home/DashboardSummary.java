package com.utilitywatch.home;

import java.time.LocalDate;
import java.util.List;

/**
 * the data displayed on the home dashboard
 *
 * @param day the day the tile totals are for (most recent day with readings)
 * @param energyTotal total energy for that day in kWh
 * @param waterTotal total water for that day in litres
 * @param trendDays one date per point on the 7 day chart (oldest first)
 * @param energyTrend daily energy totals matching trendDays
 * @param waterTrend daily water totals matching trendDays
 */
public record DashboardSummary(LocalDate day, double energyTotal, double waterTotal,
        List<LocalDate> trendDays, List<Double> energyTrend, List<Double> waterTrend) {
}