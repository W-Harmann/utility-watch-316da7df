package com.utilitywatch;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

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

    public static Connection getInstance() {
        if (instance == null) {
            new UtilityWatchConnection();
        }
        return instance;
    }

    // closes the shared connection and forgets it, so the next getInstance()
    // call opens a genuinely fresh one - used to prove data survives
    // a restart
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