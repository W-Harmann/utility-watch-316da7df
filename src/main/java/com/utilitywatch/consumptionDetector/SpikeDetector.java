package com.utilitywatch.consumptionDetector;

import com.utilitywatch.Reading;

import java.util.List;

public class SpikeDetector {

    private double baseline;
    private int consecutiveSpikes;

    public SpikeDetector(double baseline, int consecutiveSpikes) {
        this.baseline = baseline;
        this.consecutiveSpikes = consecutiveSpikes;
    }

    public boolean hasConsistentSpikes(List<Reading> readings) {

        int count = 0;

        for (Reading reading : readings) {
            if (reading.getQuantity() > baseline) {
                count++;
            }
        }

        return count >= consecutiveSpikes;
    }
}
