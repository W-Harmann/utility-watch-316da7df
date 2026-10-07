package com.utilitywatch.goals;

import java.util.List;

public class GoalManager {

    /**
     * creates a new goal and checks the resource type,
     * target reduction, and target period are valid (US8.1)
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
     */
    public List<Goal> filterActiveGoals(List<Goal> goals) {
        return goals.stream()
                .filter(goal -> !goal.isCompleted())
                .toList();
    }

    /**
     * updates the goal's progress and marks it completed when the target is reached (us8.4)
     */
    public Goal updateProgress(Goal goal, double achievedReduction) {
        boolean completed = goal.isCompleted() || achievedReduction >= goal.getTargetReduction();
        return new Goal(goal.getResourceType(), goal.getTargetReduction(), goal.getTargetPeriod(), completed, achievedReduction);
    }

    /**
     * checks if the goal has just reached its target for the first time, allows a one time notification (us8.4)
     */
    public boolean justCompleted(Goal goal, double achievedReduction) {
        return !goal.isCompleted() && achievedReduction >= goal.getTargetReduction();
    }

    /**
     * returns only completed goals so the user can view them (us8.4)
     */
    public List<Goal> filterCompletedGoals(List<Goal> goals) {
        return goals.stream()
                .filter(Goal::isCompleted)
                .toList();
    }
}