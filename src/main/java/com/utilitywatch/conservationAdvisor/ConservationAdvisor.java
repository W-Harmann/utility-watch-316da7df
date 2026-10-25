package com.utilitywatch.conservationAdvisor;

import com.utilitywatch.Reading;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * A class handling simple calculation comparisons for readings.
 */
public class ConservationAdvisor {
    /**
     * calculates a percentage change between two averages of readings.
     * @param previousPeriod The first list used in calculations.
     * @param currentPeriod The second list used in calculations.
     * @return The percentage change between two periods of readings as a double.
     */
    public Optional<Double> calculatePercentChange(Optional<List<Reading>> previousPeriod, List<Reading> currentPeriod) {

        if (previousPeriod.isPresent() && !previousPeriod.get().isEmpty() && !currentPeriod.isEmpty()) {

            double previousQuantity = 0;
            double previousAverage;
            for (Reading quantity : previousPeriod.get()) {
                previousQuantity += quantity.getQuantity();
            }

            previousAverage = previousQuantity / previousPeriod.get().size();

            double currentQuantity = 0;
            double currentAverage;
            for (Reading quantity : currentPeriod) {
                currentQuantity += quantity.getQuantity();
            }

            currentAverage = currentQuantity / currentPeriod.size();

            return Optional.of((currentAverage - previousAverage) / previousAverage * 100);
        }

        return Optional.empty();

    }

    /**
     * calculates the difference of duration of readings between two periods.
     * @param previousPeriod The first list used in calculations.
     * @param currentPeriod The second list used in calculations.
     * @return The difference of seconds between two periods of readings as a long.
     */
    public long compareDuration(List<Reading> previousPeriod, List<Reading> currentPeriod) {

        long previousSecondsPassed = 0;
        LocalDateTime lastDateTime = null;

        for (Reading reading : previousPeriod) {
            if (lastDateTime != null) {
                previousSecondsPassed += ChronoUnit.SECONDS.between(lastDateTime, reading.getDateTime());
            }

            lastDateTime = reading.getDateTime();
        }

        lastDateTime = null;
        long currentSecondsPassed = 0;

        for (Reading reading : currentPeriod) {
            if (lastDateTime != null) {
                currentSecondsPassed += ChronoUnit.SECONDS.between(lastDateTime, reading.getDateTime());
            }

            lastDateTime = reading.getDateTime();
        }


        return currentSecondsPassed - previousSecondsPassed;
    }

    /**
     * calculates a percentage change between two max of readings.
     * @param previousPeriod The first list used in calculations.
     * @param currentPeriod The second list used in calculations.
     * @return The percentage difference of two max readings between two periods of readings as a double.
     */
    public double compareConsumptionLevel(List<Reading> previousPeriod, List<Reading> currentPeriod) {


        Double previousMax = Collections.max(previousPeriod, Comparator.comparing(Reading::getQuantity)).getQuantity();

        Double currentMax = Collections.max(currentPeriod, Comparator.comparing(Reading::getQuantity)).getQuantity();

        return (currentMax - previousMax) / previousMax * 100;
    }
}
