package com.utilitywatch;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ReadingEntryTest {
    private Reading reading;


    @BeforeEach
    void setUp() {
        reading = new Reading();
    }

    @Test
    void ShouldStoreEntryAsMap() {

        reading.setQuantity(3.0);
        reading.setDate(2026, 3, 32, 12, 4, 5);

        HashMap<LocalDateTime, Double> entry = new HashMap<>();
        entry.put(LocalDateTime.of(2026, 3, 10, 12, 4, 5), 3.0);

        assertEquals(entry, reading.toString());
    }

    @Test
    void ShouldStoreEntry() {

        List<Reading> readings = new ArrayList<>();

        reading.setQuantity(3.0);
        reading.setDate(2026, 3, 10, 12, 4, 5);

        readings.add(reading);
        readings.add(new Reading(LocalDateTime.of(2026, 3, 11, 12, 30, 34), 3.5));
        readings.add(new Reading(LocalDateTime.of(2026, 3, 12, 13, 7, 31), 3.7));

        assertEquals(readings.getFirst(), reading);
    }

}
