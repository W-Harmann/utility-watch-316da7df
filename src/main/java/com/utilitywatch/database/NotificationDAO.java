package com.utilitywatch.database;

import com.utilitywatch.UtilityWatchConnection;
import com.utilitywatch.notifications.Notification;
import com.utilitywatch.notifications.Type;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Lets other classes manage notifications without knowing how they are stored.
 * Defines how notifications are added, retrieved, updated and deleted.
 */
public class NotificationDAO implements INotificationDAO {

    private final Connection connection;

    /**
     * connects to the shared database and creates the goals table if it does not already exist.
     */
    public NotificationDAO() {
        connection = UtilityWatchConnection.getInstance();
        createTable();
    }

    /**
     * creates the notifications table if it does not already exist.
     */
    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS notifications ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "userId INTEGER NOT NULL,"
                    + "notificationTime VARCHAR NOT NULL,"
                    + "title VARCHAR NOT NULL,"
                    + "message VARCHAR NOT NULL,"
                    + "ignoredStatus BOOLEAN NOT NULL,"
                    + "notificationType VARCHAR NOT NULL"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * saves a new notification for a user and writes the generated id back onto it.
     * @param notification the notification to save.
     * @param userId the id of the user who owns the notification.
     */
    @Override
    public void insert(Notification notification, int userId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO notifications (userId, notificationTime, title, message, ignoredStatus, notificationType) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, notification.getDateTime().toString());
            statement.setString(3, notification.getTitle());
            statement.setString(4, notification.getMessage());
            statement.setBoolean(5, notification.getIgnoredStatus());
            statement.setString(6, notification.getType().toString());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    notification.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * gets results of one user, newest notification first.
     * @param userId the id of the user.
     * @return that user's notifications, or an empty list if they have none.
     */
    @Override
    public List<Notification> getNotificationsForUser(int userId) {
        List<Notification> notifications = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM notifications WHERE userId = ? ORDER BY notificationTime DESC")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    notifications.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return notifications;
    }

    /**
     * updates notification by its id.
     * @param notification the notification with its new values.
     */
    @Override
    public void update(Notification notification) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE notifications SET ignoredStatus = NOT ignoredStatus WHERE id = ?")) {
            statement.setInt(1, notification.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * removes a notification by id, does nothing if it does not exist.
     * @param id the id of the notification to remove.
     */
    @Override
    public void delete(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM notifications WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * deletes a user's notifications when their account is deleted.
     * @param userId the id of the user.
     */
    @Override
    public void deleteNotificationsForUser(int userId) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM notifications WHERE userId = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * deletes a user's unusual notifications to refresh when data is updated.
     * @param userId the id of the user.
     */
    @Override
    public void deleteUnusualNotificationsForUser(int userId) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM notifications WHERE userId = ? AND notificationType = ?")) {
            statement.setInt(1, userId);
            statement.setString(2, Type.UNUSUALPATTERN.toString());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * creates a notification from a database row.
     * @param resultSet a set of results from each of the notification's stored columns.
     * @return a notification.
     * @throws SQLException If an error with the SQLite database occurs.
     */
    private Notification mapRow(ResultSet resultSet) throws SQLException {
        Notification notification = new Notification(
                LocalDateTime.parse(resultSet.getString("notificationTime")),
                resultSet.getString("title"),
                resultSet.getString("message"),
                resultSet.getBoolean("ignoredStatus"),
                Type.valueOf(resultSet.getString("notificationType")));
        notification.setId(resultSet.getInt("id"));
        return notification;
    }
}