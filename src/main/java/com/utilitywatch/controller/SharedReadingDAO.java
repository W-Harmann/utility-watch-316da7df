package com.utilitywatch.controller;

import com.utilitywatch.database.ReadingDAO;

// one shared ReadingDAO so navigating to Readings repeatedly doesn't keep
// opening new database connections
public class SharedReadingDAO {

    private static final ReadingDAO instance = new ReadingDAO();

    static {
        instance.createTable();
    }

    private SharedReadingDAO() {
    }

    public static ReadingDAO getDAO() {
        return instance;
    }
}