package com.utilitywatch.controller;

import com.utilitywatch.database.ReadingDAO;

/**
 * Holds the one shared {@link ReadingDAO}, so moving between screens does not keep opening new
 * database connections. The {@code readings} table is created when this class is first loaded.
 */
public class SharedReadingDAO {

    private static final ReadingDAO instance = new ReadingDAO();

    static {
        instance.createTable();
    }

    private SharedReadingDAO() {
    }

    /**
     * Returns the shared reading DAO.
     *
     * @return the shared DAO
     */
    public static ReadingDAO getDAO() {
        return instance;
    }
}