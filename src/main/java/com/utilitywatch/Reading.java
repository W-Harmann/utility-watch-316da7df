package com.utilitywatch;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * A single meter reading: how much of a utility was used at a point in time.
 */
public record Reading(LocalDateTime timestamp, double amount, String utility) {

    /** Used when the utility type isn't known or doesn't matter (e.g. the detectors). */
    public static final String UNSPECIFIED = "unspecified";

    public Reading {
        Objects.requireNonNull(timestamp, "timestamp is required");
        Objects.requireNonNull(utility, "utility is required");
        if (amount < 0) {
            throw new IllegalArgumentException("Reading amount cannot be negative: " + amount);
        }
    }

    /** Creates a reading without a utility type. */
    public Reading(LocalDateTime timestamp, double amount) {
        this(timestamp, amount, UNSPECIFIED);
    }
}