package com.utilitywatch.database;

import com.utilitywatch.Reading;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

// US12.3: deleting an account removes that user's readings and nobody else's
public class AccountDeletionTest {

    private static final int DELETED_USER = 987001;
    private static final int OTHER_USER = 987002;

    private ReadingDAO readingDAO;

    @BeforeEach
    void setUp() {
        readingDAO = new ReadingDAO();
        readingDAO.createTable();
    }

    @AfterEach
    void removal() {
        readingDAO.deleteReadingsForUser(DELETED_USER);
        readingDAO.deleteReadingsForUser(OTHER_USER);
        readingDAO.close();
    }

    private Reading readingFor(int userId) {
        Reading reading = new Reading(LocalDateTime.of(2026, 3, 10, 12, 0), 3.0, "energy");
        reading.setUserId(userId);
        return reading;
    }

    @Test
    void ShouldDeleteOnlyThatUsersReadings() {
        readingDAO.insert(readingFor(DELETED_USER));
        readingDAO.insert(readingFor(DELETED_USER));
        readingDAO.insert(readingFor(OTHER_USER));

        readingDAO.deleteReadingsForUser(DELETED_USER);

        assertTrue(readingDAO.getReadingsForUser(DELETED_USER).isEmpty());
        assertEquals(1, readingDAO.getReadingsForUser(OTHER_USER).size());
    }
}