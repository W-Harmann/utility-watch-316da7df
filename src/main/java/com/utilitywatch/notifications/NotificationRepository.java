package com.utilitywatch.notifications;

import com.utilitywatch.database.NotificationDAO;
import com.utilitywatch.database.INotificationDAO;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;

import java.util.ArrayList;
import java.util.List;

/**
 * manages the logged in user's notifications, keeps them in memory, and syncs changes to the database
 */
public class NotificationRepository {

    // used when no user is logged in
    private static final int NO_USER = -1;

    private static NotificationRepository instance;
    private final INotificationDAO notificationDAO;
    private final List<Notification> notifications = new ArrayList<>();
    private int loadedUserId = NO_USER;

    private NotificationRepository(INotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    /** @return the shared repository instance */
    public static NotificationRepository getInstance() {
        if (instance == null) {
            instance = new NotificationRepository(new NotificationDAO());
        }
        return instance;
    }

    /** @return a copy of the notification list that can be changed without affecting the original */
    public List<Notification> getAll() {
        loadNotificationsIfUserChanged();
        return new ArrayList<>(notifications);
    }

    /** saves a new notification for the current user  */
    public void add(Notification notification) {
        loadNotificationsIfUserChanged();
        notificationDAO.insert(notification, loadedUserId);
        notifications.add(notification);
    }

    /** updates the notification at the given position (e.g. when its progress changes) */
    public void set(int notificationIndex, Notification notification) {
        loadNotificationsIfUserChanged();
        replaceAt(notificationIndex, notification);
    }

    /** replaces an existing notification with its updated version */
    public void replace(Notification oldNotification, Notification newNotification) {
        loadNotificationsIfUserChanged();
        int notificationIndex = notifications.indexOf(oldNotification);
        if (notificationIndex >= 0) {
            replaceAt(notificationIndex, newNotification);
        }
    }

    /** removes the given notification from the list */
    public void remove(Notification notification) {
        loadNotificationsIfUserChanged();
        if (notifications.remove(notification)) {
            notificationDAO.delete(notification.getId());
        }
    }

    /** removes every notification, e.g. when an account is deleted */
    public void clear() {
        User user = Session.getInstance().getCurrentUser();
        if (user != null) {
            notificationDAO.deleteNotificationsForUser(user.getId());
        }
        notifications.clear();
        loadedUserId = NO_USER;
    }

    // keeps the database id on the new notification, then saves it
    private void replaceAt(int index, Notification newNotification) {
        newNotification.setId(notifications.get(index).getId());
        notifications.set(index, newNotification);
        notificationDAO.update(newNotification);
    }

    // reloads from the database when a different user is logged in
    private void loadNotificationsIfUserChanged() {
        User user = Session.getInstance().getCurrentUser();
        int userId = (user == null) ? NO_USER : user.getId();
        if (userId == loadedUserId) {
            return;
        }

        notifications.clear();
        if (userId != NO_USER) {
            notifications.addAll(notificationDAO.getNotificationsForUser(userId));
        }
        loadedUserId = userId;
    }
}