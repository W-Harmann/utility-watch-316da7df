package com.utilitywatch.database;

import com.utilitywatch.Reading;

import java.util.List;

public interface IReadingDAO {
    void insert(Reading reading);
    Reading getLastReading();
    List<Reading> getAllReadings();
    List<Reading> getReadingsForUser(int userId);
}