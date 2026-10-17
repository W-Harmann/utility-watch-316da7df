package com.utilitywatch.database;

import com.utilitywatch.alerts.Alert;
import java.util.List;

// lets other classes manage alerts without knowing how they are stored
// defines how alerts are added, retrieved, updated and deleted
public interface IAlertDAO {
    void insert(Alert alert, int userId);
    List<Alert> getAlertsForUser(int userId);
    void delete(int id);
    void deleteAlertsForUser(int userId);
}