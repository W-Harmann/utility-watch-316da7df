package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.util.ArrayList;
import java.util.List;

/**
 * A class handling spike detection for readings
 */
public class SpikeDetector {


    private double baseline;

    /**
     * Constructs the baseline a reading must exceed to identify spikes for the user.
     * @param baseline The baseline for spike detection.
     */
    public SpikeDetector(double baseline) {
        this.baseline = baseline;
    }

    /**
     * Determines local positive and negative spikes between two point that exceed a baseline.
     * @param readings The list of readings used in calculations, must contain three for more readings to detect spikes.
     * @return A list of readings that are spikes.
     */
    public List<Reading> hasSpikes(List<Reading> readings) {

        List<Reading> spikes = new ArrayList<>();

        if (readings.size() < 3) {
            return spikes;
        }

        for (int i = 1; i < readings.size() - 1; i++) {
            Double previous = readings.get(i - 1).getQuantity();
            Double current = readings.get(i).getQuantity();
            Double next = readings.get(i + 1).getQuantity();

            if (current >= previous * baseline && current >= next * baseline) {
                spikes.add(readings.get(i));
            }

            if (current * baseline <= previous && current * baseline <= next) {
                spikes.add(readings.get(i));
            }
        }

        return spikes;
    }
}
