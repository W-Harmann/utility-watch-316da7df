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
        return notificationDAO.getNotificationsForUser(loadedUserId);
    }

    /** saves a new notification for the current user  */
    public void add(Notification notification) {
        loadNotificationsIfUserChanged();
        notificationDAO.insert(notification, loadedUserId);
    }

    /** updates the notification at the given position (e.g. when its progress changes) */
    public void set(int notificationIndex, Notification notification) {
        loadNotificationsIfUserChanged();
    }

    /** removes every notification, e.g. when an account is deleted */
    public void clear() {
        User user = Session.getInstance().getCurrentUser();
        if (user != null) {
            notificationDAO.deleteNotificationsForUser(user.getId());
        }
        loadedUserId = NO_USER;
    }

    /** removes every unusual notification, to refresh when readings change. */
    public void clearUnusual() {
        User user = Session.getInstance().getCurrentUser();
        if (user != null) {
            notificationDAO.deleteUnusualNotificationsForUser(user.getId());
        }
    }

    // reloads from the database when a different user is logged in
    private void loadNotificationsIfUserChanged() {
        User user = Session.getInstance().getCurrentUser();
        int userId = (user == null) ? NO_USER : user.getId();
        if (userId == loadedUserId) {
            return;
        }

        if (userId != NO_USER) {
            notificationDAO.getNotificationsForUser(userId);
        }
        loadedUserId = userId;
    }
}