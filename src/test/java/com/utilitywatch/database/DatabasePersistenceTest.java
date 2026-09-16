package com.utilitywatch.database;

import org.junit.jupiter.api.*;

import java.time.LocalDateTime;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;

public class DatabasePersistenceTest {

    private ReadingDAO readingDAO;

    @BeforeEach
    void setUp() {
        readingDAO = new ReadingDAO();
        readingDAO.createTable();
    }

    @AfterEach
    void removal() {
        readingDAO.close();
    }


    @Test
    void ShouldStoreInsertedValue() {
        readingDAO.insert(new reading(LocalDateTime.of(2026, 3, 10, 12, 4, 5), 3.0));
        readingDAO.close();

        readingDAO = new ReadingDAO();
        Reading reading = readingDAO.getLastReading();

        HashMap<LocalDateTime, Double> entry = new HashMap<>();
        entry.put(LocalDateTime.of(2026, 3, 10, 12, 4, 5), 3.0);

        assertEquals(entry, reading.toString());
    }
}
