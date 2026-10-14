package com.utilitywatch.goals;

import java.util.ArrayList;
import java.util.List;

/**
 * stores the user's goals for the session so every screen sees the same list
 * TODO: replace with database persistence
 */
public class GoalRepository {

    private static final GoalRepository INSTANCE = new GoalRepository();
    private final List<Goal> goals = new ArrayList<>();

    private GoalRepository() {
    }

    /** @return the shared repository instance */
    public static GoalRepository getInstance() {
        return INSTANCE;
    }

    /** @return a copy of the goal list that can be changed without affecting the original */
    public List<Goal> getAll() {
        return new ArrayList<>(goals);
    }

    /** adds a new goal to the list */
    public void add(Goal goal) {
        goals.add(goal);
    }

    /** updates the goal at the given position (e.g. when its progress changes) */
    public void set(int goalIndex, Goal goal) {
        goals.set(goalIndex, goal);
    }

    /** replaces an existing goal with its updated version */
    public void replace(Goal oldGoal, Goal newGoal) {
        int goalIndex = goals.indexOf(oldGoal);
        if (goalIndex >= 0) {
            goals.set(goalIndex, newGoal);
        }
    }

    /** removes the given goal from the list */
    public void remove(Goal goal) {
        goals.remove(goal);
    }
}