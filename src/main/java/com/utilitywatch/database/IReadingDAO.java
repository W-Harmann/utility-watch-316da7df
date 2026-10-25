package com.utilitywatch.database;

import com.utilitywatch.Reading;

import java.util.List;

/**
 * Data access object for readings. It keeps the rest of the code independent of how readings are
 * actually stored.
 */
public interface IReadingDAO {

    /**
     * Saves a new reading and sets the generated id on it.
     *
     * @param reading the reading to save
     */
    void insert(Reading reading);

    /**
     * Returns the most recently saved reading.
     *
     * @return the most recently saved reading, whichever user owns it, or {@code null} if there are none
     */
    Reading getLastReading();

    /**
     * Returns every reading for every user.
     *
     * @return all readings, most recently saved first
     */
    List<Reading> getAllReadings();

    /**
     * Returns the readings that belong to one user, so accounts never see each other's readings.
     *
     * @param userId the id of the user
     * @return that user's readings, most recently saved first; empty if there are none
     */
    List<Reading> getReadingsForUser(int userId);

    /**
     * Overwrites the date, quantity and resource type of an existing reading, matched by its id.
     * The owning user is not changed.
     *
     * @param reading the reading with its new values
     */
    void updateReading(Reading reading);

    /**
     * Removes a reading by id. Does nothing if it does not exist.
     *
     * @param id the id of the reading to remove
     */
    void deleteReading(int id);

    /**
     * Removes every reading that belongs to a user. Used when their account is deleted.
     *
     * @param userId the id of the user
     */
    void deleteReadingsForUser(int userId);
}