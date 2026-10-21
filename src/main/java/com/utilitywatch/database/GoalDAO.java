package com.utilitywatch.database;

import com.utilitywatch.UtilityWatchConnection;
import com.utilitywatch.goals.Goal;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Saves and retrieves goals from the shared SQLite database.
 * Stores completed as 1 for yes and 0 for no.
 */
public class GoalDAO implements IGoalDAO {

    private final Connection connection;

    /** connects to the shared database and creates the goals table if it does not already exist. */
    public GoalDAO() {
        connection = UtilityWatchConnection.getInstance();
        createTable();
    }

    // creates the goals table if it does not already exist
    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS goals ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "userId INTEGER NOT NULL,"
                    + "resourceType VARCHAR NOT NULL,"
                    + "targetReduction REAL NOT NULL,"
                    + "targetPeriod VARCHAR NOT NULL,"
                    + "completed INTEGER NOT NULL DEFAULT 0,"
                    + "currentProgress REAL NOT NULL DEFAULT 0"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // saves a new goal for a user and writes the generated id back onto it
    @Override
    public void insert(Goal goal, int userId) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO goals (userId, resourceType, targetReduction, targetPeriod, completed, currentProgress) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                Statement.RETURN_GENERATED_KEYS)) {
            statement.setInt(1, userId);
            statement.setString(2, goal.getResourceType());
            statement.setDouble(3, goal.getTargetReduction());
            statement.setString(4, goal.getTargetPeriod());
            statement.setInt(5, goal.isCompleted() ? 1 : 0);
            statement.setDouble(6, goal.getCurrentProgress());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    goal.setId(generatedKeys.getInt(1));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // gets results of one user, oldest goal first
    @Override
    public List<Goal> getGoalsForUser(int userId) {
        List<Goal> goals = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT * FROM goals WHERE userId = ? ORDER BY id ASC")) {
            statement.setInt(1, userId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    goals.add(mapRow(resultSet));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return goals;
    }

    // updates a goal by its id
    @Override
    public void update(Goal goal) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE goals SET targetReduction = ?, targetPeriod = ?, completed = ?, currentProgress = ? WHERE id = ?")) {
            statement.setDouble(1, goal.getTargetReduction());
            statement.setString(2, goal.getTargetPeriod());
            statement.setInt(3, goal.isCompleted() ? 1 : 0);
            statement.setDouble(4, goal.getCurrentProgress());
            statement.setInt(5, goal.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // removes a goal by id, does nothing if it does not exist
    @Override
    public void delete(int id) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM goals WHERE id = ?")) {
            statement.setInt(1, id);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // deletes a user's goals when their account is deleted
    @Override
    public void deleteGoalsForUser(int userId) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM goals WHERE userId = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // creates a goal from a database row
    private Goal mapRow(ResultSet resultSet) throws SQLException {
        Goal goal = new Goal(
                resultSet.getString("resourceType"),
                resultSet.getDouble("targetReduction"),
                resultSet.getString("targetPeriod"),
                resultSet.getInt("completed") == 1,
                resultSet.getDouble("currentProgress"));
        goal.setId(resultSet.getInt("id"));
        return goal;
    }
}