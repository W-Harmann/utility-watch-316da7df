package com.utilitywatch.alerts;

import java.time.LocalDateTime;

public class Alert {

    private final LocalDateTime time;
    private final String title;
    private final String message;

    private int id; // assigned by the database on insert, 0 until then

    /** @return the database id, or 0 if the alert has not been saved yet */
    public int getId() {
        return id;
    }

    /** sets the database id, called by the DAO and AlertRepository */
    public void setId(int id) {
        this.id = id;
    }

    // full constructor that includes progress tracking for us8.4 and us8.5
    public Alert(LocalDateTime time, String title, String message) {
        this.time = time;
        this.title = title;
        this.message = message;
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
}