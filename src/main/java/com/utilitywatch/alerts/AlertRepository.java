package com.utilitywatch.alerts;

import com.utilitywatch.database.AlertDAO;
import com.utilitywatch.database.IAlertDAO;
import com.utilitywatch.alerts.Alert;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;

import java.util.ArrayList;
import java.util.List;

/**
 * manages the logged in user's alerts, keeps them in memory, and syncs changes to the database
 */
public class AlertRepository {

    // used when no user is logged in
    private static final int NO_USER = -1;

    private static AlertRepository instance;
    private final IAlertDAO alertDAO;
    private final List<Alert> alerts = new ArrayList<>();
    private int loadedUserId = NO_USER;

    private AlertRepository(IAlertDAO alertDAO) {
        this.alertDAO = alertDAO;
    }

    /** @return the shared repository instance */
    public static AlertRepository getInstance() {
        if (instance == null) {
            instance = new AlertRepository(new AlertDAO());
        }
        return instance;
    }

    /** @return a copy of the alert list that can be changed without affecting the original */
    public List<Alert> getAll() {
        loadAlertsIfUserChanged();
        return new ArrayList<>(alerts);
    }

    /** saves a new alert for the current user  */
    public void add(Alert alert) {
        loadAlertsIfUserChanged();
        alertDAO.insert(alert, loadedUserId);
        alerts.add(alert);
    }

    /** removes the given alert from the list */
    public void remove(Alert alert) {
        loadAlertsIfUserChanged();
        if (alerts.remove(alert)) {
            alertDAO.delete(alert.getId());
        }
    }

    /** removes every alert, e.g. when an account is deleted */
    public void clear() {
        User user = Session.getInstance().getCurrentUser();
        if (user != null) {
            alertDAO.deleteAlertsForUser(user.getId());
        }
        alerts.clear();
        loadedUserId = NO_USER;
    }

    // reloads from the database when a different user is logged in
    private void loadAlertsIfUserChanged() {
        User user = Session.getInstance().getCurrentUser();
        int userId = (user == null) ? NO_USER : user.getId();
        if (userId == loadedUserId) {
            return;
        }

        alerts.clear();
        if (userId != NO_USER) {
            alerts.addAll(alertDAO.getAlertsForUser(userId));
        }
        loadedUserId = userId;
    }
}