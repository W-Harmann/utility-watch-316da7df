package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.time.LocalTime;

public class ActiveHoursDetector {

    private LocalTime startTime;
    private LocalTime endTime;

    public ActiveHoursDetector(LocalTime startTime, LocalTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

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
