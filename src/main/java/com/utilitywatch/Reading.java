package com.utilitywatch;

import java.time.LocalDateTime;

public class Reading {

    private double quantity;
    private LocalDateTime dateTime;

    public Reading() {
    }

    public Reading(LocalDateTime dateTime, double quantity) {
        setDate(dateTime);
        setQuantity(quantity);
    }

    public void setQuantity(Object value) {
        if (!(value instanceof Number)) {
            throw new IllegalArgumentException("Quantity must be a number");
        }
        double doubleValue = ((Number) value).doubleValue();
        if (Double.isNaN(doubleValue)) {
            throw new IllegalArgumentException("Quantity cannot be NaN");
        }
        if (doubleValue < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = doubleValue;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.dateTime = dateTime;
    }

    public void setDate(int year, int month, int day, int hour, int minute, int second) {
        LocalDateTime dt = LocalDateTime.of(year, month, day, hour, minute, second);

        if (year < 1900) {
            throw new IllegalArgumentException("Date cannot be before 1900");
        }
        if (dt.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Date cannot be in the future");
        }
        this.dateTime = dt;
    }

    public void setDateTime(int year, int month, int day, int hour, int minute, int second) {
        setDate(year, month, day, hour, minute, second);
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }
}