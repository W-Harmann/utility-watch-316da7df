package com.utilitywatch.database;

import com.utilitywatch.Reading;

import java.util.List;

// keeps consumers independent of how readings are actually stored
public interface IReadingDAO {
    void insert(Reading reading);
    Reading getLastReading();
    List<Reading> getAllReadings();
    List<Reading> getReadingsForUser(int userId);
    void updateReading(Reading reading);
    void deleteReading(int id);
    void deleteReadingsForUser(int userId);
}