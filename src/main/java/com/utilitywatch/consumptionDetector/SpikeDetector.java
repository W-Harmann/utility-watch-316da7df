package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.util.ArrayList;
import java.util.List;

public class SpikeDetector {

    private double baseline;

    public SpikeDetector(double baseline) {
        this.baseline = baseline;
    }

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
