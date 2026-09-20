package com.utilitywatch.reports;

import com.utilitywatch.Reading;
import com.utilitywatch.controller.SharedReadingDAO;
import com.utilitywatch.login.Session;

import java.time.LocalDate;
import java.util.List;

// handles fetching and filtering the users readings for reports data
public class ReportsDataService {

    // gets the id of the user who is currently logged in and finds only that user's readings
    private int getCurrentUserId() {
        return Session.getInstance().getCurrentUser().getId();
    }

    // gets the user's full readings for a resource type within the selected period
    public List<Reading> getReadings(String resourceType, LocalDate periodStart, LocalDate periodEnd) {
        List<Reading> allUserReadings = SharedReadingDAO.getDAO().getReadingsForUser(getCurrentUserId());
        return ReadingFilter.byTypeAndPeriod(allUserReadings, resourceType, periodStart, periodEnd);
    }

    // gets all readings for the current user then filters them by resource type and the selected date period
    // only the reading quantities are returned as numbers and these values are used by the consumption summary and report generator
    public List<Double> getReadingQuantities(String resourceType, LocalDate periodStart, LocalDate periodEnd) {
        List<Reading> allUserReadings = SharedReadingDAO.getDAO().getReadingsForUser(getCurrentUserId());
        List<Reading> filtered = ReadingFilter.byTypeAndPeriod(allUserReadings, resourceType, periodStart, periodEnd);
        return filtered.stream().map(Reading::getQuantity).toList();
    }
}
