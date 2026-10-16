package com.utilitywatch.goals;

import com.utilitywatch.database.GoalDAO;
import com.utilitywatch.database.IGoalDAO;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;

import java.util.ArrayList;
import java.util.List;

/**
 * manages the logged in user's goals, keeps them in memory, and syncs changes to the database
 */
public class GoalRepository {

    // used when no user is logged in
    private static final int NO_USER = -1;

    private static GoalRepository instance;
    private final IGoalDAO goalDAO;
    private final List<Goal> goals = new ArrayList<>();
    private int loadedUserId = NO_USER;

    private GoalRepository(IGoalDAO goalDAO) {
        this.goalDAO = goalDAO;
    }

    /** @return the shared repository instance */
    public static GoalRepository getInstance() {
        if (instance == null) {
            instance = new GoalRepository(new GoalDAO());
        }
        return instance;
    }

    /** @return a copy of the goal list that can be changed without affecting the original */
    public List<Goal> getAll() {
        loadGoalsIfUserChanged();
        return new ArrayList<>(goals);
    }

    /** saves a new goal for the current user  */
    public void add(Goal goal) {
        loadGoalsIfUserChanged();
        goalDAO.insert(goal, loadedUserId);
        goals.add(goal);
    }

    /** updates the goal at the given position (e.g. when its progress changes) */
    public void set(int goalIndex, Goal goal) {
        loadGoalsIfUserChanged();
        replaceAt(goalIndex, goal);
    }

    /** replaces an existing goal with its updated version */
    public void replace(Goal oldGoal, Goal newGoal) {
        loadGoalsIfUserChanged();
        int goalIndex = goals.indexOf(oldGoal);
        if (goalIndex >= 0) {
            replaceAt(goalIndex, newGoal);
        }
    }

    /** removes the given goal from the list */
    public void remove(Goal goal) {
        loadGoalsIfUserChanged();
        if (goals.remove(goal)) {
            goalDAO.delete(goal.getId());
        }
    }

    /** removes every goal, e.g. when an account is deleted */
    public void clear() {
        User user = Session.getInstance().getCurrentUser();
        if (user != null) {
            goalDAO.deleteGoalsForUser(user.getId());
        }
        goals.clear();
        loadedUserId = NO_USER;
    }

    // keeps the database id on the new goal, then saves it
    private void replaceAt(int index, Goal newGoal) {
        newGoal.setId(goals.get(index).getId());
        goals.set(index, newGoal);
        goalDAO.update(newGoal);
    }

    // reloads from the database when a different user is logged in
    private void loadGoalsIfUserChanged() {
        User user = Session.getInstance().getCurrentUser();
        int userId = (user == null) ? NO_USER : user.getId();
        if (userId == loadedUserId) {
            return;
        }

        goals.clear();
        if (userId != NO_USER) {
            goals.addAll(goalDAO.getGoalsForUser(userId));
        }
        loadedUserId = userId;
    }
}