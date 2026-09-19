package com.utilitywatch;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * US11.1 - Exports readings, costs and rates for a utility and date range to a CSV file,
 * so a landlord can share expected costs with potential tenants.
 * Only usage data is written: never user IDs, emails or credentials.
 */
public class Exporter extends CostBudgetEvaluator {

    /** The outcome of an export: whether it worked, and a message for the user. */
    public record ExportResult(boolean success, String message) {
    }

    public ExportResult export(String selection, Path file) {
        List<String> utilities = resolveSelection(selection);

        if (!getTimeRange().isEmpty() && !getTimeRangeValidity()) {
            return new ExportResult(false, "'To' date must be on or after 'From' date");
        }

        List<Reading> included = readingsToExport(utilities);
        if (included.isEmpty()) {
            return new ExportResult(false, "No readings in the selected period");
        }

        String content = buildCsv(selection, utilities, included);
        return writeSafely(file, content);
    }

    private List<Reading> readingsToExport(List<String> utilities) {
        List<LocalDateTime> range = getTimeRange();
        List<Reading> included = new ArrayList<>();
        for (Reading reading : getReadings()) {
            boolean utilityMatches = utilities.contains(reading.getResourceType());
            boolean inRange = range.isEmpty()
                    || (!reading.getDateTime().isBefore(range.get(0)) && !reading.getDateTime().isAfter(range.get(1)));
            if (utilityMatches && inRange) {
                included.add(reading);
            }
        }
        included.sort(Comparator.comparing(Reading::getDateTime));
        return included;
    }

    private String buildCsv(String selection, List<String> utilities, List<Reading> readings) {
        StringBuilder csv = new StringBuilder();
        LocalDateTime from = getTimeRange().isEmpty() ? readings.get(0).getDateTime() : getTimeRange().get(0);
        LocalDateTime to = getTimeRange().isEmpty() ? readings.get(readings.size() - 1).getDateTime() : getTimeRange().get(1);

        csv.append("UtilityWatch usage export\n");
        csv.append("Period,").append(from.toLocalDate()).append(',').append(to.toLocalDate()).append('\n');
        csv.append("Utility selection,").append(selection).append("\n\n");

        // Only usage fields - no ids, user ids, emails or credentials
        csv.append("Date,Utility,Quantity,Unit\n");
        for (Reading reading : readings) {
            csv.append(reading.getDateTime().toLocalDate()).append(',')
                    .append(reading.getResourceType()).append(',')
                    .append(BigDecimal.valueOf(reading.getQuantity()).toPlainString()).append(',')
                    .append(reading.getUnit()).append('\n');
        }

        csv.append("\nUtility,Rate ($ per unit),Cost ($)\n");
        for (String utility : utilities) {
            csv.append(utility).append(',')
                    .append(BigDecimal.valueOf(getUtilityCost(utility)).toPlainString()).append(',')
                    .append(calculateUtilityCostExact(utility).toPlainString()).append('\n');
        }

        BigDecimal total = calculateUtilityCostExact(selection);
        long months = ChronoUnit.MONTHS.between(YearMonth.from(from), YearMonth.from(to)) + 1;
        BigDecimal averagePerMonth = total.divide(BigDecimal.valueOf(months), 2, MONEY_ROUNDING);

        csv.append("Total,,").append(total.toPlainString()).append('\n');
        csv.append("Average per month (").append(months).append(months == 1 ? " month" : " months")
                .append("),,").append(averagePerMonth.toPlainString()).append('\n');
        return csv.toString();
    }

    /** Writes to a temporary file first, then moves it into place, so a failure never leaves a partial file. */
    private ExportResult writeSafely(Path file, String content) {
        Path target = file.toAbsolutePath();
        Path temp = null;
        try {
            temp = Files.createTempFile(target.getParent(), "export-", ".tmp");
            Files.writeString(temp, content);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new ExportResult(true, "Exported to " + target);
        } catch (IOException e) {
            return new ExportResult(false, "Could not write file: " + describe(e));
        } finally {
            if (temp != null) {
                try {
                    Files.deleteIfExists(temp);
                } catch (IOException ignored) {
                    // nothing more we can do
                }
            }
        }
    }

    private static String describe(IOException e) {
        String name = e.getClass().getSimpleName().replace("Exception", "");
        return e.getMessage() == null ? name : name + " (" + e.getMessage() + ")";
    }
}
