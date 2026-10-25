package com.utilitywatch.database;

import com.utilitywatch.Reading;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Imports readings from CSV text that follows the agreed schema.
 * <p>The first line is a header and is skipped. Every following line has four columns:
 * {@code date, resource_type, quantity, unit}, for example {@code 2026-01-01, energy, 12.5, kWh}.
 * Rows are checked one at a time. Valid rows are saved through an {@link IReadingDAO}, and invalid
 * rows are skipped and recorded in an {@link ImportReport}.
 */
public class CsvReadingImportService {

    private final IReadingDAO readingDAO;

    /**
     * Creates an importer that saves valid readings through the given DAO.
     *
     * @param readingDAO where valid readings are saved
     */
    public CsvReadingImportService(IReadingDAO readingDAO) {
        this.readingDAO = readingDAO;
    }

    /**
     * Parses and imports every valid row, tagged to the given user. Invalid rows are skipped and
     * recorded in the returned report, so one bad row never aborts the whole import.
     * <p>A row is skipped if it does not have exactly four columns, has an invalid date or quantity,
     * has a resource type other than energy or water, has a unit that does not match the type
     * (kWh for energy, litres for water), or is refused by {@link com.utilitywatch.Reading}
     * (for example a negative quantity). Blank lines are ignored. Row numbers in the messages are
     * line numbers in the file, with the header counted as row 1.
     *
     * @param csvContent the whole file as text, header line first
     * @param userId the id of the user the readings are saved for
     * @return how many rows were saved, and a message for each row that was skipped
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

    /** The outcome of an import: how many rows were saved, and a message for each row that was skipped. */
    public static class ImportReport {
        private final int successCount;
        private final List<String> failureMessages;

        /**
         * Creates a report.
         *
         * @param successCount how many rows were saved
         * @param failureMessages one message per skipped row
         */
        public ImportReport(int successCount, List<String> failureMessages) {
            this.successCount = successCount;
            this.failureMessages = failureMessages;
        }

        /**
         * Returns how many rows were saved.
         *
         * @return the number of readings saved
         */
        public int getSuccessCount() {
            return successCount;
        }

        /**
         * Returns the reason each skipped row was skipped, for example
         * {@code "Row 6: Quantity cannot be negative"}. The size of the list is the number of skipped rows.
         *
         * @return one message per skipped row
         */
        public List<String> getFailureMessages() {
            return failureMessages;
        }
    }
}