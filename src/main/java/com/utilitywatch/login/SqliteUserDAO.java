package com.utilitywatch.login;

import com.utilitywatch.UtilityWatchConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class SqliteUserDAO implements IUserDAO {

    private final Connection connection;

    public SqliteUserDAO() {
        connection = UtilityWatchConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name VARCHAR,"
                    + "email VARCHAR NOT NULL UNIQUE,"
                    + "passwordHash VARCHAR NOT NULL,"
                    + "propertyAddress VARCHAR"
                    + ")");
            addNameColumnIfMissing();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // databases created before the profile feature have no name column, and CREATE TABLE IF NOT EXISTS
    // won't add it, so add it here
    private void addNameColumnIfMissing() throws SQLException {
        boolean hasName = false;
        try (Statement statement = connection.createStatement();
             ResultSet columns = statement.executeQuery("PRAGMA table_info(users)")) {
            while (columns.next()) {
                if ("name".equals(columns.getString("name"))) {
                    hasName = true;
                }
            }
        }
        if (!hasName) {
            try (Statement statement = connection.createStatement()) {
                statement.execute("ALTER TABLE users ADD COLUMN name VARCHAR");
            }
        }
    }

    @Override
    public void register(User user) {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO users (name, email, passwordHash, propertyAddress) VALUES (?, ?, ?, ?)")) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPropertyAddress());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    user.setId(generatedKeys.getInt(1));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findByEmail(String email) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?")) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    User user = new User(resultSet.getString("email"), "");
                    user.setId(resultSet.getInt("id"));
                    user.setPassword(resultSet.getString("passwordHash")); // overwrite - don't re-hash the stored value
                    user.setName(resultSet.getString("name"));
                    user.setPropertyAddress(resultSet.getString("propertyAddress"));
                    return user;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // saves the user's current details so they survive an app restart
    @Override
    public void update(User user) {
        try (PreparedStatement statement = connection.prepareStatement(
                "UPDATE users SET name = ?, email = ?, passwordHash = ?, propertyAddress = ? WHERE id = ?")) {
            statement.setString(1, user.getName());
            statement.setString(2, user.getEmail());
            statement.setString(3, user.getPassword());
            statement.setString(4, user.getPropertyAddress());
            statement.setInt(5, user.getId());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // removes the account; does nothing if it doesn't exist
    @Override
    public void delete(int userId) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM users WHERE id = ?")) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}