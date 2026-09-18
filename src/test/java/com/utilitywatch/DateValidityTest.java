package com.utilitywatch;

import org.junit.jupiter.api.*;

import java.time.DateTimeException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class DateValidityTest {
    private Reading reading;

    @BeforeEach
    void setUp() {
        reading = new Reading();
    }

    @Test
    void ShouldThrowExceptionAtNull() {
        assertThrows(IllegalArgumentException.class, () -> reading.setDate(null));
    }

    @Test
    void ShouldThrowExceptionAtDateBefore1900() {
        assertThrows(IllegalArgumentException.class, () -> reading.setDate(1899, 12, 31, 23, 59, 59));
    }

    @Test
    void ShouldThrowExceptionAtDateInFuture() {
        assertThrows(IllegalArgumentException.class, () -> reading.setDate(3000, 1, 1, 1, 1, 1));
    }

    @Test
    void ShouldThrowExceptionAtIncorrectDate() {
        assertThrows(DateTimeException.class, () -> reading.setDate(2026, 3, 32, 12, 4, 5));
    }

    @Test
    void ShouldStoreDate() {
        reading.setDateTime(2026, 9, 10, 13, 4, 6);
        assertEquals(LocalDateTime.of(2026, 9, 10, 13, 4, 6), reading.getDateTime());
    }

    @Test
    void ShouldStoreLeapYearDate() {
        reading.setDateTime(2024, 2, 29, 11, 55, 23);
        assertEquals(LocalDateTime.of(2024, 2, 29, 11, 55, 23), reading.getDateTime());
    }
}