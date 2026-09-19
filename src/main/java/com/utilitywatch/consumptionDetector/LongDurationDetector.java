package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class LongDurationDetector {

    private double baseline;
    private int consecutiveHours;

    public LongDurationDetector(double baseline, int consecutiveHours) {
        this.baseline = baseline;
        this.consecutiveHours = consecutiveHours;
    }

    public boolean hasLongDurationUsage(List<Reading> readings) {

        long secondsPassed = 0;
        LocalDateTime lastDateTime = null;

        for (Reading reading : readings) {
            if (reading.getQuantity() > baseline) {


                if (lastDateTime != null) {
                    secondsPassed += ChronoUnit.SECONDS.between(lastDateTime, reading.getDateTime());
                }

                lastDateTime = reading.getDateTime();
            }
        }

        int hoursPassed = (int) secondsPassed / 60 / 60;

        return hoursPassed >= consecutiveHours;
    }
}
