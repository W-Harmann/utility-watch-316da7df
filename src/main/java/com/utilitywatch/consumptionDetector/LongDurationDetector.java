package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class LongDurationDetector {

    private double baseline;
    private int consecutiveHours;

    public LongDurationDetector(double baseline, int consecutiveHours) {
        this.baseline = baseline;
        this.consecutiveHours = consecutiveHours;
    }

    public List<List<Reading>> hasLongDurationUsage(List<Reading> readings) {

        long secondsPassed = 0;
        int hoursPassed;
        LocalDateTime lastDateTime = null;
        List<Reading> consecutiveReadingsAboveBaseline = new ArrayList<>();
        List<List<Reading>> allFlaggedReadingGroups = new ArrayList<>();


        for (Reading reading : readings) {
            if (reading.getQuantity() > baseline) {

                if (lastDateTime != null) {
                    secondsPassed += ChronoUnit.SECONDS.between(lastDateTime, reading.getDateTime());

                }

                hoursPassed = (int) secondsPassed / 60 / 60;

                System.out.println(reading.getQuantity() + " + " + hoursPassed);

                consecutiveReadingsAboveBaseline.add(reading);
            }
            else {


                hoursPassed = (int) secondsPassed / 60 / 60;

                System.out.println(reading.getQuantity() + " + " + hoursPassed);

                if (hoursPassed >= consecutiveHours && ChronoUnit.HOURS.between(lastDateTime, reading.getDateTime()) < consecutiveHours) {
                    allFlaggedReadingGroups.add(consecutiveReadingsAboveBaseline);
                }

                consecutiveReadingsAboveBaseline = new ArrayList<>();

                secondsPassed = 0;

            }

            lastDateTime = reading.getDateTime();
        }

        hoursPassed = (int) secondsPassed / 60 / 60;

        if (hoursPassed >= consecutiveHours) {
            allFlaggedReadingGroups.add(consecutiveReadingsAboveBaseline);
        }

        return allFlaggedReadingGroups;
    }
}
