package com.utilitywatch.goals;

// US8.1 - As a user, I want to create a waste reduction goal so that I
// can work towards reducing my energy or water consumption.
//
// US8.2 - As a user, I want to view my active reduction goals so that
// I can keep track of what I am trying to achieve.

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.util.List;

class GoalManagerTest {

    private GoalManager goalManager;

    @BeforeEach
    void setUp() {
        goalManager = new GoalManager();
    }

    // AC: "The goal is saved when all required values are valid" (also
    // covers selecting energy/water, entering a target reduction, and
    // selecting a target period, for the valid case)
    @Test
    void shouldCreateGoalWhenAllValuesAreValid() {
        Goal goal = goalManager.createGoal("energy", 10.0, "monthly");

        assertEquals("energy", goal.getResourceType());
        assertEquals(10.0, goal.getTargetReduction(), 0.01);
        assertEquals("monthly", goal.getTargetPeriod());
    }

    // AC: "The system validates that the entered values are valid" —
    // covers an invalid resource type
    @Test
    void shouldRejectGoalWithInvalidResourceType() {
        assertThrows(IllegalArgumentException.class,
                () -> goalManager.createGoal("gas", 10.0, "monthly"));
    }

    // AC: "The system validates that the entered values are valid" —
    // covers a negative target reduction
    @Test
    void shouldRejectGoalWithNegativeTargetReduction() {
        assertThrows(IllegalArgumentException.class,
                () -> goalManager.createGoal("energy", -5.0, "monthly"));
    }

    // AC: "The system validates that the entered values are valid" —
    // covers a missing target period
    @Test
    void shouldRejectGoalWithMissingTargetPeriod() {
        assertThrows(IllegalArgumentException.class,
                () -> goalManager.createGoal("energy", 10.0, null));
    }

    // AC: "The system displays an error message if any values are invalid"
    @Test
    void shouldProvideErrorMessageWhenValuesAreInvalid() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> goalManager.createGoal("gas", 10.0, "monthly"));

        assertNotNull(exception.getMessage());
        assertFalse(exception.getMessage().isEmpty());
    }

    // AC: "The system displays the user's active goals" / "each goal
    // identifies the relevant resource" / "each goal displays the target
    // and target period" — checked together on the same returned goal,
    // since these all describe what one active goal's data contains
    @Test
    void shouldReturnActiveGoalsWithTheirData() {
        List<Goal> goals = List.of(
                new Goal("energy", 10.0, "monthly", false),
                new Goal("water", 20.0, "weekly", true)
        );

        List<Goal> activeGoals = goalManager.filterActiveGoals(goals);

        assertEquals(1, activeGoals.size());
        Goal activeGoal = activeGoals.get(0);
        assertEquals("energy", activeGoal.getResourceType());
        assertEquals(10.0, activeGoal.getTargetReduction(), 0.01);
        assertEquals("monthly", activeGoal.getTargetPeriod());
    }

    // AC: "The user can distinguish between active and completed goals"
    @Test
    void shouldDistinguishActiveFromCompletedGoals() {
        Goal activeGoal = new Goal("energy", 10.0, "monthly", false);
        Goal completedGoal = new Goal("water", 20.0, "weekly", true);

        assertFalse(activeGoal.isCompleted());
        assertTrue(completedGoal.isCompleted());
    }
}