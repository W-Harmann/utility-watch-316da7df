package com.utilitywatch.goals;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.OptionalDouble;

// US8.3 - As a user, I want to see my progress towards my reduction
// goals so that I can determine whether I am on track to achieve them

public class GoalProgressCalculatorTest {

    private GoalProgressCalculator calculator;

    @BeforeEach
    void setUp() {
        calculator = new GoalProgressCalculator();
    }

    // AC: the system compares the user's current consumption with their target reduction goal
    @Test
    void shouldCalculateReductionPercentBetweenPeriods() {
        OptionalDouble reduction = calculator.reductionPercent(100.0, 80.0);

        assertTrue(reduction.isPresent());
        assertEquals(20.0, reduction.getAsDouble(), 0.01);
    }

    @Test
    void shouldReportZeroReductionWhenConsumptionIncreased() {
        OptionalDouble reduction = calculator.reductionPercent(100.0, 130.0);

        assertEquals(0.0, reduction.getAsDouble(), 0.01);
    }

    @Test
    void shouldReportNoComparisonWhenPreviousPeriodHasNoConsumption() {
        assertTrue(calculator.reductionPercent(0.0, 50.0).isEmpty());
    }

    // AC: the system displays the user's progress towards their goal
    @Test
    void shouldCalculateProgressFractionTowardsTarget() {
        assertEquals(0.5, calculator.progressFraction(5.0, 10.0), 0.01);
    }

    @Test
    void shouldCapProgressFractionAtOneWhenTargetExceeded() {
        assertEquals(1.0, calculator.progressFraction(25.0, 10.0), 0.01);
    }

    @Test
    void shouldReportZeroProgressWhenNothingAchieved() {
        assertEquals(0.0, calculator.progressFraction(0.0, 10.0), 0.01);
    }

    @Test
    void shouldTreatZeroTargetAsFullyAchieved() {
        assertEquals(1.0, calculator.progressFraction(0.0, 0.0), 0.01);
    }

    // AC: the system indicates whether the user is on track to meet their goal
    @Test
    void shouldReportCompletedWhenTargetReached() {
        assertEquals(GoalProgressCalculator.Status.COMPLETED, calculator.status(10.0, 10.0));
    }

    @Test
    void shouldReportOnTrackWhenHalfwayOrMore() {
        assertEquals(GoalProgressCalculator.Status.ON_TRACK, calculator.status(5.0, 10.0));
    }

    @Test
    void shouldReportBehindWhenBelowHalfway() {
        assertEquals(GoalProgressCalculator.Status.BEHIND, calculator.status(4.0, 10.0));
    }

    // AC: the user's progress updates when new consumption data is recorded
    // the same goal gives a different status when consumption decreases
    @Test
    void shouldGiveHigherProgressWhenNewConsumptionDataLowersUsage() {
        double before = calculator.reductionPercent(100.0, 95.0).getAsDouble();
        double after = calculator.reductionPercent(100.0, 70.0).getAsDouble();

        assertTrue(calculator.progressFraction(after, 20.0) > calculator.progressFraction(before, 20.0));
        assertEquals(GoalProgressCalculator.Status.BEHIND, calculator.status(before, 20.0));
        assertEquals(GoalProgressCalculator.Status.COMPLETED, calculator.status(after, 20.0));

    }
}

