package com.utilitywatch.goals;

import java.util.List;

/**
 * Handles creating, editing, deleting, tracking and filtering reduction goals.
 * Contains no JavaFX for easy testing. Changes return a new goal because
 * existing goals cannot be changed after creation.
 */
public class GoalManager {

    /**
     * creates a new goal and checks the resource type, target reduction, and target period are valid (US8.1).
     * new goals start as not completed.
     * @param resourceType energy or water
     * @param targetReduction the percentage reduction to achieve, which must not be negative
     * @param targetPeriod the period label for the goal, which must not be empty
     * @return the new goal
     * @throws IllegalArgumentException if any value is invalid
     */
    public Goal createGoal(String resourceType, double targetReduction, String targetPeriod) {
        // resource must be only energy or water
        if (!"energy".equals(resourceType) && !"water".equals(resourceType)) {
            throw new IllegalArgumentException("Resource type must be 'energy' or 'water'");
        }

        // target reduction cannot be negative
        if (targetReduction < 0) {
            throw new IllegalArgumentException("Target reduction must not be negative");
        }

        // target period cannot be empty
        if (targetPeriod == null || targetPeriod.isBlank()) {
            throw new IllegalArgumentException("Target period must not be empty");
        }

        // new goals start as not completed
        return new Goal(resourceType, targetReduction, targetPeriod, false);
    }

    /**
     * returns only the goals that are not completed yet / still active (US8.2)
     * @param goals the goals to filter
     * @return the goals that are not completed
     */
    public List<Goal> filterActiveGoals(List<Goal> goals) {
        return goals.stream()
                .filter(goal -> !goal.isCompleted())
                .toList();
    }

    /**
     * updates the goal's progress and marks it completed when the target is reached (us8.4).
     * a goal that is already completed stays completed.
     * @param goal the goal to update
     * @param achievedReduction the percentage reduction achieved so far
     * @return a new goal with the updated progress and completion status
     */
    public Goal updateProgress(Goal goal, double achievedReduction) {
        boolean completed = goal.isCompleted() || achievedReduction >= goal.getTargetReduction();
        return new Goal(goal.getResourceType(), goal.getTargetReduction(), goal.getTargetPeriod(), completed, achievedReduction);
    }

    /**
     * checks if the goal has just reached its target for the first time, allows a one time notification (us8.4)
     * @param goal the goal to check
     * @param achievedReduction the percentage reduction achieved so far
     * @return true only when the goal is not yet completed and the target has now been met
     */
    public boolean justCompleted(Goal goal, double achievedReduction) {
        return !goal.isCompleted() && achievedReduction >= goal.getTargetReduction();
    }

    /**
     * returns only completed goals so the user can view them (us8.4)
     * @param goals the goals to filter
     * @return the goals that are completed
     */
    public List<Goal> filterCompletedGoals(List<Goal> goals) {
        return goals.stream()
                .filter(Goal::isCompleted)
                .toList();
    }

    /**
     * edits an existing goal with a new target and period.
     * validates the new values and updates completion based on current progress (us8.5).
     * the resource type cannot be changed.
     * @param goal the goal to edit
     * @param newTargetReduction the new percentage reduction, which must not be negative
     * @param newTargetPeriod the new period label, which must not be empty
     * @return a new goal with the changes applied
     * @throws IllegalArgumentException if the new target or period is invalid
     */
    public Goal editGoal(Goal goal, double newTargetReduction, String newTargetPeriod) {
        if (newTargetReduction < 0) {
            throw new IllegalArgumentException("Target reduction must not be negative");
        }

        if (newTargetPeriod == null || newTargetPeriod.isBlank()) {
            throw new IllegalArgumentException("Target period must not be empty");
        }

        // rechecks completion using the goal's existing progress (raising target reopens already completed goal)
        boolean completed = goal.getCurrentProgress() >= newTargetReduction;

        return new Goal(goal.getResourceType(), newTargetReduction, newTargetPeriod, completed, goal.getCurrentProgress());
    }

    /**
     * removes the selected goal and returns the updated list of goals (us8.5)
     * @param goals the current goals
     * @param goalToDelete the goal to remove
     * @return a new list without the deleted goal
     */
    public List<Goal> deleteGoal(List<Goal> goals, Goal goalToDelete) {
        List<Goal> updatedGoals = new java.util.ArrayList<>(goals);
        updatedGoals.remove(goalToDelete);
        return updatedGoals;
    }
}