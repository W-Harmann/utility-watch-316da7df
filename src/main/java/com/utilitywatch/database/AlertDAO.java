package com.utilitywatch.database;

import com.utilitywatch.UtilityWatchConnection;
import com.utilitywatch.alerts.Alert;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// handles saving and retrieving alerts from the shared SQLite database
public class AlertDAO implements IAlertDAO {

    private final Connection connection;

    public AlertDAO() {
        connection = UtilityWatchConnection.getInstance();
        createTable();
    }

    // creates the alerts table if it does not already exist
    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS alerts ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "userId INTEGER NOT NULL,"
                    + "alertTime VARCHAR NOT NULL,"
                    + "title VARCHAR NOT NULL,"
                    + "message VARCHAR NOT NULL,"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // saves a new alert for a user and writes the generated id back onto it
    @Override
    public void insert(Alert alert, int userId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO alerts (userId, alertTime, title, message) "
                        + "VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, alert.getDateTime().toString());
            statement.setString(3, alert.getTitle());
            statement.setString(4, alert.getMessage());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    alert.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // gets results of one user, oldest alert first
    @Override
    public List<Alert> getAlertsForUser(int userId) {
        List<Alert> alerts = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM alerts WHERE userId = ? ORDER BY id ASC")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    alerts.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return alerts;
    }

    // removes a alert by id, does nothing if it does not exist
    @Override
    public void delete(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM alerts WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // delets a user's alerts when their account is deleted
    @Override
    public void deleteAlertsForUser(int userId) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM alerts WHERE userId = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // creates an alert from a database row
    private Alert mapRow(ResultSet resultSet) throws SQLException {
        Alert alert = new Alert(
                LocalDateTime.parse(resultSet.getString("alertTime")),
                resultSet.getString("title"),
                resultSet.getString("message"));
        alert.setId(resultSet.getInt("id"));
        return alert;
    }
}