package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * A class handling long duration detection for readings
 */
public class LongDurationDetector {

    private double baseline;
    private int consecutiveHours;

    /**
     * Constructs the baseline a reading must exceed and the consecutive hours it must last for the user.
     * @param baseline The baseline reading quantity must exceed consecutively.
     * @param consecutiveHours The hours reading duration must exceed consecutively.
     */
    public LongDurationDetector(double baseline, int consecutiveHours) {
        this.baseline = baseline;
        this.consecutiveHours = consecutiveHours;
    }

    /**
     * Determines if a list of readings has both exceeded baseline and hours consecutively.
     * @param readings The list of readings used in calculations.
     * @return A list containing groups of readings if each reading exceeds high usage and if the group exceeds long duration.
     */
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

                consecutiveReadingsAboveBaseline.add(reading);
            }
            else {


                hoursPassed = (int) secondsPassed / 60 / 60;

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
