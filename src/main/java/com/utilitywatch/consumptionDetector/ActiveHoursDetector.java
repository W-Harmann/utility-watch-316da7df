package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.time.LocalTime;

/**
 * A class handling active hours detection for readings.
 */
public class ActiveHoursDetector {

    private LocalTime startTime;
    private LocalTime endTime;

    /**
     * Constructs the active hours of the user.
     * @param startTime The starting time the user is active.
     * @param endTime The ending time the user is active.
     */
    public ActiveHoursDetector(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    /**
     * Determines if a reading exceeds an overnight baseline outside active hours.
     * @param current The current reading used for calculations.
     * @param normalOvernightBaseline The overnight baseline used for calculations.
     * @return true if the reading exceeds overnight baseline outside active hours.
     */
    public boolean isUnusual(Reading current, double normalOvernightBaseline) {
        LocalTime currentTime = current.getDateTime().toLocalTime();

        if (startTime.isBefore(endTime)) {
            if (currentTime.isBefore(startTime) || currentTime.isAfter(endTime)) {
                return current.getQuantity() >= normalOvernightBaseline * 1.25;
            }
        }
        else {
            if (currentTime.isBefore(startTime) && currentTime.isAfter(endTime)) {
                return current.getQuantity() >= normalOvernightBaseline * 1.25;
            }
        }

        return false;

    }
}
