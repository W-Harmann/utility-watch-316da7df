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
                    "INSERT INTO readings (dateTime, quantity, resourceType, userId) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, reading.getDateTime().toString());
            statement.setDouble(2, reading.getQuantity());
            statement.setString(3, reading.getResourceType());
            statement.setInt(4, reading.getUserId());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                reading.setId(generatedKeys.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void updateReading(Reading reading) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "UPDATE readings SET dateTime = ?, quantity = ?, resourceType = ? WHERE id = ?");
            statement.setString(1, reading.getDateTime().toString());
            statement.setDouble(2, reading.getQuantity());
            statement.setString(3, reading.getResourceType());
            statement.setInt(4, reading.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteReading(int id) {
        try {
            PreparedStatement statement = connection.prepareStatement("DELETE FROM readings WHERE id = ?");
            statement.setInt(1, id);
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
        reading.setId(resultSet.getInt("id"));
        return reading;
    }

    public void close() {
        UtilityWatchConnection.reset();
    }
}