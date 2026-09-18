package com.utilitywatch.database;

import com.utilitywatch.Reading;
import org.junit.jupiter.api.*;

import java.time.LocalDateTime;

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
        readingDAO.insert(new Reading(LocalDateTime.of(2026, 3, 10, 12, 4, 5), 3.0));
        readingDAO.close();

        readingDAO = new ReadingDAO();
        Reading reading = readingDAO.getLastReading();

        assertEquals(LocalDateTime.of(2026, 3, 10, 12, 4, 5), reading.getDateTime());
        assertEquals(3.0, reading.getQuantity());
    }
}