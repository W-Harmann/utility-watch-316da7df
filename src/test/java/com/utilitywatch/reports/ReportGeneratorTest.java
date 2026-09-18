package com.utilitywatch.reports;

// US7.2 - As a user, I want to view a detailed report of my energy and
// water consumption so that I can analyse my usage over a selected period.

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;
import java.util.Optional;

public class ReportGeneratorTest {

    private ReportGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new ReportGenerator();
    }

    // AC: "The report includes analysis of usage, such as trends, comparisons,
    // or unusual patterns, where available" — this covers the "available" case
    @Test
    void shouldReturnComparisonWhenPreviousPeriodDataAvailable() {
        Optional<List<Double>> previousPeriod = Optional.of(List.of(10.0, 10.0));
        List<Double> currentPeriod = List.of(13.0, 13.0);

        Optional<Double> comparison = generator.compareToPreviousPeriod(previousPeriod, currentPeriod);

        assertTrue(comparison.isPresent());
    }

    // AC: "The report includes analysis of usage, such as trends, comparisons,
    // or unusual patterns, where available" — this covers the "not available" case
    @Test
    void shouldReturnEmptyComparisonWhenNoPreviousPeriodDataAvailable() {
        Optional<List<Double>> previousPeriod = Optional.empty();
        List<Double> currentPeriod = List.of(13.0, 13.0);

        Optional<Double> comparison = generator.compareToPreviousPeriod(previousPeriod, currentPeriod);

        assertTrue(comparison.isEmpty());
    }

    // AC: "The user is informed if there is insufficient data to generate a
    // meaningful report" — zero readings is the unambiguous insufficient case
    @Test
    void shouldConsiderZeroReadingsInsufficientData() {
        List<Double> readings = List.of();

        boolean sufficient = generator.hasSufficientData(readings);

        assertFalse(sufficient);
    }

    // AC: "The user is informed if there is insufficient data to generate a
    // meaningful report" — confirms recorded data is not flagged as insufficient
    @Test
    void shouldConsiderAnyRecordedReadingsSufficientData() {
        List<Double> readings = List.of(10.0);

        boolean sufficient = generator.hasSufficientData(readings);

        assertTrue(sufficient);
    }
}