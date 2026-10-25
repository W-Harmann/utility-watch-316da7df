package com.utilitywatch.database;

import com.utilitywatch.notifications.Notification;
import com.utilitywatch.notifications.Type;

import java.util.List;

/**
 * Lets other classes manage notifications without knowing how they are stored.
 * Defines how notifications are added, retrieved, updated and deleted.
 */
public interface INotificationDAO {
    /**
     * saves a new notification and adds its generated id to the notification.
     * @param notification the notification to save.
     * @param userId the id of the user who owns the notification.
     */
    void insert(Notification notification, int userId);

    /**
     * gets one user's notifications sorted by newest first.
     * @param userId the id of the user.
     * @return that user's notifications, or an empty list if they have none.
     */
    List<Notification> getNotificationsForUser(int userId);

    /**
     * updates a saved notification's ignoredStatus, matched by its id.
     * @param notification the notification with its new values.
     */
    void update(Notification notification);

    /**
     * removes a notification by id, does nothing if it does not exist.
     * @param id the id of the notification to remove.
     */
    void delete(int id);

    /**
     * removes every notification belonging to a user, e.g. when their account is deleted.
     * @param userId the id of the user.
     */
    void deleteNotificationsForUser(int userId);

    /**
     * removes every unusual notification belonging to a user, e.g. when data is refreshed.
     * @param userId the id of the user.
     */
    void deleteUnusualNotificationsForUser(int userId);
}