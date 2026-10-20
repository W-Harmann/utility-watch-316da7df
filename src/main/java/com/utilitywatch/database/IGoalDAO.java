package com.utilitywatch.database;

import com.utilitywatch.goals.Goal;
import java.util.List;

/**
 * Lets other classes manage goals without knowing how they are stored.
 * Defines how goals are added, retrieved, updated and deleted.
 */
public interface IGoalDAO {
    /**
     * saves a new goal and adds its generated id to the goal
     * @param goal the goal to save
     * @param userId the id of the user who owns the goal
     */
    void insert(Goal goal, int userId);

    /**
     * gets one user's goals sorted by oldest first
     * @param userId the id of the user
     * @return that user's goals, or an empty list if they have none
     */
    List<Goal> getGoalsForUser(int userId);

    /**
     * updates a saved goal's target, period, completion and progress, matched by its id
     * @param goal the goal with its new values
     */
    void update(Goal goal);

    /**
     * removes a goal by id, does nothing if it does not exist
     * @param id the id of the goal to remove
     */
    void delete(int id);

    /**
     * removes every goal belonging to a user, e.g. when their account is deleted
     * @param userId the id of the user
     */
    void deleteGoalsForUser(int userId);
}