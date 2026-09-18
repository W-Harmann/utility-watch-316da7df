package com.utilitywatch;

import java.time.LocalDateTime;

public class Reading {

    private int id; // assigned by the DAO on insert, 0 until then
    private int userId;
    private double quantity;
    private LocalDateTime dateTime;
    private String resourceType; // "energy" or "water"

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
        // don't catch this - an invalid day like 32 should just throw
        // DateTimeException on its own
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

    /** @throws IllegalArgumentException if not exactly "energy" or "water" */
    public void setResourceType(String resourceType) {
        if (!"energy".equals(resourceType) && !"water".equals(resourceType)) {
            throw new IllegalArgumentException("Resource type must be 'energy' or 'water'");
        }
        this.resourceType = resourceType;
    }

    public String getResourceType() {
        return resourceType;
    }

    public String getUnit() {
        return "energy".equals(resourceType) ? "kWh" : "litres";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}