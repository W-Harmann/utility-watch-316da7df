package com.utilitywatch.database;

import com.utilitywatch.notifications.Notification;

import java.util.List;

// lets other classes manage notifications without knowing how they are stored
// defines how notifications are added, retrieved, updated and deleted
public interface INotificationDAO {
    void insert(Notification notification, int userId);
    List<Notification> getNotificationsForUser(int userId);
    void update(Notification notification);
    void delete(int id);
    void deleteNotificationsForUser(int userId);
}