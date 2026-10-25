package com.utilitywatch.database;

import com.utilitywatch.Reading;
import com.utilitywatch.UtilityWatchConnection;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite implementation of {@link IReadingDAO}. Saves and retrieves readings in the {@code readings}
 * table of the shared database.
 * <p>Every method rethrows a {@link java.sql.SQLException} as a {@link RuntimeException}.
 */
public class ReadingDAO implements IReadingDAO {

    private final Connection connection;

    /**
     * Creates a DAO that uses the shared database connection. It does not create the table; call
     * {@link #createTable()} first.
     */
    public ReadingDAO() {
        connection = UtilityWatchConnection.getInstance();
    }

    /**
     * Creates the {@code readings} table if it does not already exist.
     * The resource type column allows nulls because some inserts (such as the original persistence
     * test) do not set one.
     *
     * @throws RuntimeException if the table cannot be created
     */
    public void createTable() {
        try (Statement statement = connection.createStatement()) {
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

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the reading cannot be saved
     */
    public void insert(Reading reading) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO readings (dateTime, quantity, resourceType, userId) VALUES (?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, reading.getDateTime().toString());
            statement.setDouble(2, reading.getQuantity());
            statement.setString(3, reading.getResourceType());
            statement.setInt(4, reading.getUserId());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    reading.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the update fails
     */
    public void updateReading(Reading reading) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE readings SET dateTime = ?, quantity = ?, resourceType = ? WHERE id = ?")) {
            statement.setString(1, reading.getDateTime().toString());
            statement.setDouble(2, reading.getQuantity());
            statement.setString(3, reading.getResourceType());
            statement.setInt(4, reading.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the delete fails
     */
    public void deleteReading(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM readings WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the delete fails
     */
    public void deleteReadingsForUser(int userId) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM readings WHERE userId = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the query fails
     */
    public Reading getLastReading() {
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM readings ORDER BY id DESC LIMIT 1")) {
            if (resultSet.next()) {
                return mapRow(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the query fails
     */
    public List<Reading> getAllReadings() {
        List<Reading> readings = new ArrayList<>();
        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT * FROM readings ORDER BY id DESC")) {
            while (resultSet.next()) {
                readings.add(mapRow(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return readings;
    }

    /**
     * {@inheritDoc}
     *
     * @throws RuntimeException if the query fails
     */
    public List<Reading> getReadingsForUser(int userId) {
        List<Reading> readings = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM readings WHERE userId = ? ORDER BY id DESC")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    readings.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return readings;
    }

    // builds a Reading from one row of the result set
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

    /**
     * Closes the shared connection so that a later {@code new ReadingDAO()} opens a genuinely fresh
     * one. See {@link com.utilitywatch.UtilityWatchConnection#reset()} for the caveat.
     */
    public void close() {
        UtilityWatchConnection.reset();
    }
}