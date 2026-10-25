package com.utilitywatch;

import java.time.LocalDateTime;

/**
 * A single utility reading: an amount of energy (kWh) or water (litres) recorded at a date and time
 * for one user.
 * <p>The setters validate their input and throw {@link IllegalArgumentException} for bad values: a
 * negative or NaN quantity, a null date, or a resource type other than {@code "energy"} or
 * {@code "water"}. The same rules apply however a reading is created, whether through the
 * Readings form or the CSV import.
 */
public final class Reading {

    private int id; // assigned by the DAO on insert, 0 until then
    private int userId;
    private double quantity;
    private LocalDateTime dateTime;
    private String resourceType; // "energy" or "water"

    /** Creates a reading with no date, resource type or user. Fill these in with the setters. */
    public Reading() {
    }

    /**
     * Creates a reading with a date and a quantity. The resource type is left unset.
     *
     * @param dateTime when the reading was taken
     * @param quantity the amount used
     * @throws IllegalArgumentException if the date is null, or the quantity is negative or NaN
     */
    public Reading(LocalDateTime dateTime, double quantity) {
        setDate(dateTime);
        setQuantity(quantity);
    }

    /**
     * Creates a reading with a date, a quantity and a resource type.
     *
     * @param dateTime when the reading was taken
     * @param quantity the amount used
     * @param resourceType {@code "energy"} or {@code "water"}
     * @throws IllegalArgumentException if the date is null, the quantity is negative or NaN,
     *                                  or the resource type is not {@code "energy"} or {@code "water"}
     */
    public Reading(LocalDateTime dateTime, double quantity, String resourceType) {
        this(dateTime, quantity);
        setResourceType(resourceType);
    }

    /**
     * Sets the quantity used.
     *
     * @param value the quantity; must be a {@link Number} that is neither NaN nor negative
     * @throws IllegalArgumentException if {@code value} is not a number, is NaN, or is negative
     */
    public void setQuantity(Object value) {
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException("Quantity must be a number");
        }
        double doubleValue = number.doubleValue();
        if (Double.isNaN(doubleValue)) {
            throw new IllegalArgumentException("Quantity cannot be NaN");
        }
        if (doubleValue < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }
        this.quantity = doubleValue;
    }

    /**
     * Returns the quantity used.
     *
     * @return the quantity, in the unit given by {@link #getUnit()}
     */
    public double getQuantity() {
        return quantity;
    }

    /**
     * Sets the date and time of the reading.
     * <p>Unlike {@link #setDate(int, int, int, int, int, int)}, this version does not reject dates in
     * the future or before 1900.
     *
     * @param dateTime when the reading was taken
     * @throws IllegalArgumentException if {@code dateTime} is null
     */
    public void setDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        this.dateTime = dateTime;
    }

    /**
     * Sets the date and time from separate parts.
     *
     * @param year the year; must not be before 1900
     * @param month the month, 1 to 12
     * @param day the day of the month
     * @param hour the hour, 0 to 23
     * @param minute the minute, 0 to 59
     * @param second the second, 0 to 59
     * @throws java.time.DateTimeException if the parts do not make a valid date and time (for example day 32)
     * @throws IllegalArgumentException if the year is before 1900, or the date and time are in the future
     */
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

    /**
     * Sets the date and time from separate parts. Does the same as
     * {@link #setDate(int, int, int, int, int, int)}.
     *
     * @param year the year; must not be before 1900
     * @param month the month, 1 to 12
     * @param day the day of the month
     * @param hour the hour, 0 to 23
     * @param minute the minute, 0 to 59
     * @param second the second, 0 to 59
     * @throws java.time.DateTimeException if the parts do not make a valid date and time
     * @throws IllegalArgumentException if the year is before 1900, or the date and time are in the future
     */
    public void setDateTime(int year, int month, int day, int hour, int minute, int second) {
        setDate(year, month, day, hour, minute, second);
    }

    /**
     * Returns the date and time of the reading.
     *
     * @return when the reading was taken, or {@code null} if no date has been set
     */
    public LocalDateTime getDateTime() {
        return dateTime;
    }

    /**
     * Sets the resource type.
     *
     * @param resourceType exactly {@code "energy"} or {@code "water"}
     * @throws IllegalArgumentException if it is not exactly {@code "energy"} or {@code "water"}
     */
    public void setResourceType(String resourceType) {
        if (!"energy".equals(resourceType) && !"water".equals(resourceType)) {
            throw new IllegalArgumentException("Resource type must be 'energy' or 'water'");
        }
        this.resourceType = resourceType;
    }

    /**
     * Returns the resource type.
     *
     * @return {@code "energy"} or {@code "water"}, or {@code null} if none has been set
     */
    public String getResourceType() {
        return resourceType;
    }

    /**
     * Returns the unit that goes with the resource type.
     *
     * @return {@code "kWh"} for energy, otherwise {@code "litres"} (including when no type is set)
     */
    public String getUnit() {
        return "energy".equals(resourceType) ? "kWh" : "litres";
    }

    /**
     * Returns the database id.
     *
     * @return the id given by the DAO when the reading was saved, or 0 if it has not been saved yet
     */
    public int getId() {
        return id;
    }

    /**
     * Sets the database id. The DAO calls this after saving a reading and when it loads one.
     *
     * @param id the database id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Returns the id of the user who owns this reading.
     *
     * @return the owning user's id
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the id of the user who owns this reading.
     *
     * @param userId the owning user's id
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }
}