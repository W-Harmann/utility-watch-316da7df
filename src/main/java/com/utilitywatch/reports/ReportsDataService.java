package com.utilitywatch.reports;

import com.utilitywatch.Reading;
import com.utilitywatch.controller.SharedReadingDAO;
import com.utilitywatch.login.Session;

import java.time.LocalDate;
import java.util.List;

/**
 * Fetches the logged-in user's readings for the reports, filtered by resource type and date range.
 * Readings come from the shared reading DAO, and only the current user's readings are used.
 */
public class ReportsDataService {

    // gets the id of the user who is currently logged in and finds only that user's readings
    private int getCurrentUserId() {
        return Session.getInstance().getCurrentUser().getId();
    }

    /**
     * Gets the current user's readings of one resource type within a period.
     *
     * @param resourceType {@code "energy"} or {@code "water"}
     * @param periodStart the first day of the period (inclusive)
     * @param periodEnd the last day of the period (inclusive)
     * @return the matching readings
     * @throws NullPointerException if nobody is logged in
     */
    public List<Reading> getReadings(String resourceType, LocalDate periodStart, LocalDate periodEnd) {
        List<Reading> allUserReadings = SharedReadingDAO.getDAO().getReadingsForUser(getCurrentUserId());
        return ReadingFilter.byTypeAndPeriod(allUserReadings, resourceType, periodStart, periodEnd);
    }

    /**
     * Gets just the quantities of the current user's readings of one resource type within a period.
     * These numbers are what the {@link ConsumptionSummaryCalculator} and {@link ReportGenerator} work on.
     *
     * @param resourceType {@code "energy"} or {@code "water"}
     * @param periodStart the first day of the period (inclusive)
     * @param periodEnd the last day of the period (inclusive)
     * @return the quantity of each matching reading
     * @throws NullPointerException if nobody is logged in
     */
    public List<Double> getReadingQuantities(String resourceType, LocalDate periodStart, LocalDate periodEnd) {
        List<Reading> allUserReadings = SharedReadingDAO.getDAO().getReadingsForUser(getCurrentUserId());
        List<Reading> filtered = ReadingFilter.byTypeAndPeriod(allUserReadings, resourceType, periodStart, periodEnd);
        return filtered.stream().map(Reading::getQuantity).toList();
    }
}
