package com.utilitywatch.database;

import com.utilitywatch.Reading;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports readings from a CSV file matching the agreed schema:
 *   date, resource_type, quantity, unit
 * e.g. 2026-01-01, energy, 12.5, kWh
 */
public class CsvReadingImportService {

    private final IReadingDAO readingDAO;

    public CsvReadingImportService(IReadingDAO readingDAO) {
        this.readingDAO = readingDAO;
    }

    /**
     * Parses and imports every valid row, tagged to the given user. Invalid
     * rows are skipped and recorded in the returned report - one bad row
     * never aborts the whole import.
     */
    public ImportReport importCsv(String csvContent, int userId) {
        List<String> failures = new ArrayList<>();
        int successCount = 0;

        String[] lines = csvContent.split("\\R");
        for (int i = 1; i < lines.length; i++) { // line 0 is the header row
            String line = lines[i].trim();
            if (line.isEmpty()) continue;

            int rowNumber = i + 1;
            String[] columns = line.split(",");

            if (columns.length != 4) {
                failures.add("Row " + rowNumber + ": expected 4 columns, found " + columns.length);
                continue;
            }

            try {
                LocalDate date = LocalDate.parse(columns[0].trim());
                String resourceType = columns[1].trim();
                double quantity = Double.parseDouble(columns[2].trim());
                String unit = columns[3].trim();

                String expectedUnit = "energy".equals(resourceType) ? "kWh"
                        : "water".equals(resourceType) ? "litres" : null;
                if (expectedUnit == null) {
                    failures.add("Row " + rowNumber + ": unknown resource_type '" + resourceType + "'");
                    continue;
                }
                if (!unit.equalsIgnoreCase(expectedUnit)) {
                    failures.add("Row " + rowNumber + ": unit '" + unit + "' does not match resource_type '"
                            + resourceType + "' (expected " + expectedUnit + ")");
                    continue;
                }

                Reading reading = new Reading(date.atStartOfDay(), quantity);
                reading.setResourceType(resourceType);
                reading.setUserId(userId);
                readingDAO.insert(reading);
                successCount++;

            } catch (DateTimeParseException e) {
                failures.add("Row " + rowNumber + ": invalid date '" + columns[0].trim() + "'");
            } catch (NumberFormatException e) {
                failures.add("Row " + rowNumber + ": invalid quantity '" + columns[2].trim() + "'");
            } catch (IllegalArgumentException e) {
                failures.add("Row " + rowNumber + ": " + e.getMessage());
            }
        }

        return new ImportReport(successCount, failures);
    }

    public static class ImportReport {
        private final int successCount;
        private final List<String> failureMessages;

        public ImportReport(int successCount, List<String> failureMessages) {
            this.successCount = successCount;
            this.failureMessages = failureMessages;
        }

        public int getSuccessCount() {
            return successCount;
        }

        public List<String> getFailureMessages() {
            return failureMessages;
        }
    }
}