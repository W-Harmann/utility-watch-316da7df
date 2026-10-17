package com.utilitywatch.notifications;

import java.time.LocalDateTime;

public class Notification {

    private final LocalDateTime time;
    private final String title;
    private final String message;
    private final boolean ignored;

    private int id; // assigned by the database on insert, 0 until then

    /** @return the database id, or 0 if the notification has not been saved yet */
    public int getId() {
        return id;
    }

    /** sets the database id, called by the DAO and NotificationRepository */
    public void setId(int id) {
        this.id = id;
    }

    // full constructor that includes progress tracking for us8.4 and us8.5
    public Notification(LocalDateTime time, String title, String message, boolean ignored) {
        this.time = time;
        this.title = title;
        this.message = message;
        this.ignored = ignored;
    }

    public LocalDateTime getDateTime() {
        return time;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public boolean getIgnoredStatus() {
        return ignored;
    }
}