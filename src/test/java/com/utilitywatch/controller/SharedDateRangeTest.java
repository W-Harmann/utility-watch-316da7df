package com.utilitywatch.controller;

import com.utilitywatch.Reading;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SharedDateRangeTest {

    private final List<Reading> readings = List.of(
            new Reading(LocalDateTime.of(2026, 3, 10, 0, 0), 5, "energy"),
            new Reading(LocalDateTime.of(2026, 1, 5, 0, 0), 5, "water"),
            new Reading(LocalDateTime.of(2026, 2, 20, 0, 0), 5, "energy"));

    @BeforeEach
    void setUp() {
        SharedDateRange.clear();
    }

    @AfterEach
    void tearDown() {
        SharedDateRange.clear();
    }

    @Test
    void ShouldDefaultToFirstAndLatestReading() {
        SharedDateRange.useDefaultIfNotChosen(readings);
        assertEquals(LocalDate.of(2026, 1, 5), SharedDateRange.getFrom());
        assertEquals(LocalDate.of(2026, 3, 10), SharedDateRange.getTo());
    }

    @Test
    void ShouldDefaultToLast30DaysWithNoReadings() {
        SharedDateRange.useDefaultIfNotChosen(List.of());
        assertEquals(LocalDate.now(), SharedDateRange.getTo());
        assertEquals(LocalDate.now().minusDays(30), SharedDateRange.getFrom());
    }

    @Test
    void ShouldKeepRangeChosenByUser() {
        SharedDateRange.set(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
        SharedDateRange.useDefaultIfNotChosen(readings);
        assertEquals(LocalDate.of(2026, 2, 1), SharedDateRange.getFrom());
        assertEquals(LocalDate.of(2026, 2, 28), SharedDateRange.getTo());
    }

    @Test
    void ShouldIgnoreBackwardsOrIncompleteRange() {
        SharedDateRange.set(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
        SharedDateRange.set(LocalDate.of(2026, 3, 1), LocalDate.of(2026, 2, 1));
        SharedDateRange.set(null, LocalDate.of(2026, 2, 1));
        assertEquals(LocalDate.of(2026, 2, 1), SharedDateRange.getFrom());
        assertEquals(LocalDate.of(2026, 2, 28), SharedDateRange.getTo());
    }

    @Test
    void ShouldUseDefaultAgainAfterClear() {
        SharedDateRange.set(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 28));
        SharedDateRange.clear();
        SharedDateRange.useDefaultIfNotChosen(readings);
        assertEquals(LocalDate.of(2026, 1, 5), SharedDateRange.getFrom());
    }

    @Test
    void ShouldIncludeNewReadingsWhileNotChosenByUser() {
        SharedDateRange.useDefaultIfNotChosen(readings);
        List<Reading> more = List.of(new Reading(LocalDateTime.of(2026, 4, 1, 0, 0), 5, "energy"));
        SharedDateRange.useDefaultIfNotChosen(more);
        assertEquals(LocalDate.of(2026, 4, 1), SharedDateRange.getTo());
    }
}
