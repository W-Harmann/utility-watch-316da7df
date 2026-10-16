package com.utilitywatch.database;

import com.utilitywatch.goals.Goal;
import java.util.List;

// lets other classes manage goals without knowing how they are stored
// defines how goals are added, retrieved, updated and deleted
public interface IGoalDAO {
    void insert(Goal goal, int userId);
    List<Goal> getGoalsForUser(int userId);
    void update(Goal goal);
    void delete(int id);
    void deleteGoalsForUser(int userId);
}