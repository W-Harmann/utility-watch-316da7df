package com.utilitywatch.database;

import com.utilitywatch.Reading;
import com.utilitywatch.UtilityWatchConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ReadingDAO implements IReadingDAO {

    private final Connection connection;

    public ReadingDAO() {
        connection = UtilityWatchConnection.getInstance();
    }

    public void createTable() {
        try {
            Statement statement = connection.createStatement();
            // resourceType is nullable - older/simple inserts (like the
            // original persistence test) don't set one, and that's fine.
            // userId defaults to 0 for the same reason - it's an int, so it
            // never comes back as null from Java's side either way.
            statement.execute("CREATE TABLE IF NOT EXISTS readings ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "dateTime VARCHAR NOT NULL,"
                    + "quantity REAL NOT NULL,"
                    + "resourceType VARCHAR,"
                    + "userId INTEGER NOT NULL DEFAULT 0"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void insert(Reading reading) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO readings (dateTime, quantity, resourceType, userId) VALUES (?, ?, ?, ?)");
            statement.setString(1, reading.getDateTime().toString());
            statement.setDouble(2, reading.getQuantity());
            statement.setString(3, reading.getResourceType()); // may be null
            statement.setInt(4, reading.getUserId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public Reading getLastReading() {
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(
                    "SELECT * FROM readings ORDER BY id DESC LIMIT 1");
            if (resultSet.next()) {
                return mapRow(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public List<Reading> getAllReadings() {
        List<Reading> readings = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery("SELECT * FROM readings ORDER BY id DESC");
            while (resultSet.next()) {
                readings.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return readings;
    }

    public List<Reading> getReadingsForUser(int userId) {
        List<Reading> readings = new ArrayList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "SELECT * FROM readings WHERE userId = ? ORDER BY id DESC");
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                readings.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return readings;
    }

    private Reading mapRow(ResultSet resultSet) throws SQLException {
        LocalDateTime dateTime = LocalDateTime.parse(resultSet.getString("dateTime"));
        double quantity = resultSet.getDouble("quantity");
        Reading reading = new Reading(dateTime, quantity);

        String resourceType = resultSet.getString("resourceType");
        if (resourceType != null) {
            reading.setResourceType(resourceType);
        }
        reading.setUserId(resultSet.getInt("userId"));
        return reading;
    }

    // closes the shared connection so a later "new ReadingDAO()" opens a
    // genuinely fresh one - see UtilityWatchConnection.reset() for the caveat
    public void close() {
        UtilityWatchConnection.reset();
    }
}