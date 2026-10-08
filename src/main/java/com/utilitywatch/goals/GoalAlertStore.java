package com.utilitywatch.goals;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// stores completed goal alerts for the alerts screen
// TODO: replace with database persistence
public class GoalAlertStore {
    public record GoalAlert(LocalDateTime time, String title, String message) {}

    private static final List<GoalAlert> alerts = new ArrayList<>();

    public static void add(GoalAlert alert) {
        alerts.add(alert);
    }

    public static List<GoalAlert> getAll() {
        return List.copyOf(alerts);
    }
}