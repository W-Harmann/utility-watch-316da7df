package com.utilitywatch.goals;

// US8.1 (p0) - As a user, I want to create a waste reduction goal so that I
// can work towards reducing my energy or water consumption.
//
// US8.2 (p0) - As a user, I want to view my active reduction goals so that
// I can keep track of what I am trying to achieve.
//
// US8.4 (p1) - As a user, I want to be informed when I achieve a reduction
// goal so that I can recognise my progress in reducing consumption.
//
// US8.5 (p1) - As a user, I want to edit or remove my reduction goals so
// that I can keep my goals relevant to my current circumstances.

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
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

    // --------------------------------- //
    // us8.4 - goal completion
    @Test
    void shouldMarkGoalCompletedWhenTargetReductionAchieved() {
        Goal goal = goalManager.createGoal("energy", 15.0, "3 Months");
        Goal updated = goalManager.updateProgress(goal, 15.0);
        assertTrue(updated.isCompleted());
    }

    @Test
    void shouldNotMarkGoalCompletedWhenBelowTarget() {
        Goal goal = goalManager.createGoal("energy", 20.0, "6 Months");
        Goal updated = goalManager.updateProgress(goal, 8.0);
        assertFalse(updated.isCompleted());
    }

    @Test
    void shouldNotifyCompletedGoal() {
        Goal goal = goalManager.createGoal("energy", 15.0, "3 Months");
        boolean justCompleted = goalManager.justCompleted(goal, 15.0);
        assertTrue(justCompleted);
    }

    @Test
    void shouldNotNotifyCompletedIfGoalAlreadyCompleted() {
        Goal goal = goalManager.createGoal("energy", 15.0, "3 Months");
        Goal alreadyCompleted = goalManager.updateProgress(goal, 15.0);
        boolean justCompleted = goalManager.justCompleted(alreadyCompleted, 18.0);
        assertFalse(justCompleted);
    }

    @Test
    void shouldReturnOnlyCompletedGoals() {
        Goal active = goalManager.createGoal("energy", 15.0, "3 Months");
        Goal completed = goalManager.updateProgress(goalManager.createGoal("water", 10.0, "1 Month"), 10.0);
        List<Goal> completedGoals = goalManager.filterCompletedGoals(List.of(active, completed));
        assertEquals(1, completedGoals.size());
        assertTrue(completedGoals.get(0).isCompleted());
    }

    // --------------------------------- //
    // us8.5 - edit / delete goals
    @Test
    void shouldUpdateTargetAndPeriodWhenEditingGoal() {
        Goal original = goalManager.createGoal("energy", 15.0, "3 Months");

        Goal edited = goalManager.editGoal(original, 20.0, "6 Months");

        assertEquals(20.0, edited.getTargetReduction(), 0.01);
        assertEquals("6 Months", edited.getTargetPeriod());
        assertEquals("energy", edited.getResourceType());
    }

    @Test
    void shouldRejectEditWithInvalidTarget() {
        Goal original = goalManager.createGoal("energy", 15.0, "3 Months");

        assertThrows(IllegalArgumentException.class,
                () -> goalManager.editGoal(original, -5.0, "6 Months"));
    }

    @Test
    void shouldRejectEditWithEmptyPeriod() {
        Goal original = goalManager.createGoal("energy", 15.0, "3 Months");

        assertThrows(IllegalArgumentException.class,
                () -> goalManager.editGoal(original, 20.0, ""));
    }

    @Test
    void shouldReevaluateCompletionWhenTargetIsRaisedOnEdit() {
        Goal goal = goalManager.createGoal("energy", 10.0, "3 Months");
        Goal completed = goalManager.updateProgress(goal, 10.0);

        Goal edited = goalManager.editGoal(completed, 20.0, "3 Months");

        assertFalse(edited.isCompleted());
    }

    @Test
    void shouldRemoveGoalFromListWhenDeleted() {
        Goal goalToKeep = goalManager.createGoal("energy", 15.0, "3 Months");
        Goal goalToDelete = goalManager.createGoal("water", 10.0, "1 Month");
        List<Goal> goals = new ArrayList<>(List.of(goalToKeep, goalToDelete));

        List<Goal> result = goalManager.deleteGoal(goals, goalToDelete);

        assertEquals(1, result.size());
        assertTrue(result.contains(goalToKeep));
        assertFalse(result.contains(goalToDelete));
    }
}