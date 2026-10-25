package com.utilitywatch.notifications;

import java.time.LocalDateTime;

/**
 * A simple model class representing a notification with a time, title, message, ignored status, and type.
 */
public class Notification {

    private final LocalDateTime time;
    private final String title;
    private final String message;
    private final boolean ignored;
    private final Type type;

    private int id; // assigned by the database on insert, 0 until then

    /** @return the database id, or 0 if the notification has not been saved yet */
    public int getId() {
        return id;
    }

    /** sets the database id, called by the DAO and NotificationRepository */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Constructs a new Notification with the specified time, title, message, ignored status, and type.
     * @param time The time the notification occurred.
     * @param title The title of the notification.
     * @param message The message of the notification.
     * @param ignored The ignored status of the notification.
     * @param type The type of the notification
     */
    public Notification(LocalDateTime time, String title, String message, boolean ignored, Type type) {
        this.time = time;
        this.title = title;
        this.message = message;
        this.ignored = ignored;
        this.type = type;
    }

    /**
     * @return the localDateTime time of the notification
     */
    public LocalDateTime getDateTime() {
        return time;
    }

    /**
     * @return the string title of the notification
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the string message of the notification
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the boolean ignoredStatus of the notification
     */
    public boolean getIgnoredStatus() {
        return ignored;
    }

    /**
     * @return the type of the notification
     */
    public Type getType() {
        return type;
    }
}