package com.utilitywatch.login;

import com.utilitywatch.UtilityWatchConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

public class SqliteUserDAO implements IUserDAO {

    private final Connection connection;

    public SqliteUserDAO() {
        connection = UtilityWatchConnection.getInstance();
        createTable();
    }

    private void createTable() {
        try {
            Statement statement = connection.createStatement();
            statement.execute("CREATE TABLE IF NOT EXISTS users ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "email VARCHAR NOT NULL UNIQUE,"
                    + "passwordHash VARCHAR NOT NULL,"
                    + "propertyAddress VARCHAR"
                    + ")");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void register(User user) {
        try {
            PreparedStatement statement = connection.prepareStatement(
                    "INSERT INTO users (email, passwordHash, propertyAddress) VALUES (?, ?, ?)");
            statement.setString(1, user.getEmail());
            statement.setString(2, user.getPassword());
            statement.setString(3, user.getPropertyAddress());
            statement.executeUpdate();

            ResultSet generatedKeys = statement.getGeneratedKeys();
            if (generatedKeys.next()) {
                user.setId(generatedKeys.getInt(1));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public User findByEmail(String email) {
        try {
            PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            statement.setString(1, email);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = new User(resultSet.getString("email"), "");
                user.setId(resultSet.getInt("id"));
                user.setPassword(resultSet.getString("passwordHash")); // overwrite - don't re-hash the stored value
                user.setPropertyAddress(resultSet.getString("propertyAddress"));
                return user;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}