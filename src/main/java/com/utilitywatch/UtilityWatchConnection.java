package com.utilitywatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Provides the single SQLite connection shared by every DAO.
 * <p>The connection to {@code utilitywatch.db} is opened the first time {@link #getInstance()} is
 * called and then reused, so all DAOs talk to the same database through one connection. If the
 * connection cannot be opened, the error is printed and {@code getInstance()} returns {@code null}.
 */
public class UtilityWatchConnection {

    private static Connection instance = null;

    private UtilityWatchConnection() {
        String url = "jdbc:sqlite:utilitywatch.db";
        try {
            instance = DriverManager.getConnection(url);
        } catch (SQLException sqlEx) {
            System.err.println(sqlEx);
        }
    }

    /**
     * Returns the shared database connection, opening it first if it is not open yet.
     *
     * @return the open connection, or {@code null} if it could not be opened
     */
    public static Connection getInstance() {
        if (instance == null) {
            new UtilityWatchConnection();
        }
        return instance;
    }

    /**
     * Closes the shared connection and forgets it, so the next call to {@link #getInstance()}
     * opens a genuinely fresh one. Used to prove that data survives an app restart.
     * <p>DAOs keep the connection they were created with, so any DAO created before a reset is left
     * holding the closed connection. Create new DAOs after calling this.
     */
    public static void reset() {
        try {
            if (instance != null) {
                instance.close();
            }
        } catch (SQLException e) {
            System.err.println(e);
        } finally {
            instance = null;
        }
    }
}