package com.utilitywatch.reports;

import com.utilitywatch.Exporter.ExportResult;
import com.utilitywatch.reports.CustomReport.Section;
import com.utilitywatch.reports.CustomReport.Table;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * US7.3 / US11.2 - saves a CustomReport as a CSV file (for spreadsheets) or a plain text file (for reading/printing).
 * Like the US11.1 exporter, only usage data is written: never user IDs, emails or credentials.
 */
public class CustomReportWriter {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM yyyy");
    private static final String TITLE = "UtilityWatch Consumption Report";

    /**
     * Writes the report to a file, as CSV unless the file name ends in {@code .txt}. An existing file
     * is replaced.
     *
     * @param report the report to save
     * @param file where to save it
     * @return a successful result with the saved path, or an unsuccessful one with the reason
     *         (the report has no readings, or the file could not be written)
     */
    public ExportResult write(CustomReport report, Path file) {
        if (!report.hasReadings()) {
            return new ExportResult(false, "No readings in the selected period, so there is nothing to export");
        }
        String name = file.getFileName() == null ? "" : file.getFileName().toString().toLowerCase(Locale.ROOT);
        String content = name.endsWith(".txt") ? toText(report) : toCsv(report);
        return writeSafely(file, content);
    }

    // ---- CSV ----

    /**
     * Turns the report into CSV text. It starts with a byte order mark so Excel reads it correctly,
     * then a title and summary lines, then each section with its tables and notes.
     *
     * @param report the report to convert
     * @return the CSV text
     */
    public String toCsv(CustomReport report) {
        StringBuilder csv = new StringBuilder();
        csv.append('﻿'); // byte order mark so Excel reads the dashes and units correctly
        csv.append(TITLE).append('\n');
        csv.append("Utility,").append(cell(report.utilityLabel())).append('\n');
        csv.append("Period,").append(report.from()).append(',').append(report.to()).append('\n');
        csv.append("Generated,").append(report.generatedOn()).append('\n');
        csv.append("Included,").append(cell(String.join("; ", report.sections().stream().map(Section::title).toList())))
                .append('\n');

        for (Section section : report.sections()) {
            csv.append('\n').append(cell(section.title().toUpperCase(Locale.ROOT))).append('\n');
            for (Table table : section.tables()) {
                csv.append(cell(table.heading())).append('\n');
                csv.append(row(table.columns())).append('\n');
                for (List<String> row : table.rows()) {
                    csv.append(row(row)).append('\n');
                }
                csv.append('\n');
            }
            for (String note : section.notes()) {
                csv.append(cell("Note: " + note)).append('\n');
            }
        }
        return csv.toString();
    }

    private static String row(List<String> cells) {
        return String.join(",", cells.stream().map(CustomReportWriter::cell).toList());
    }

    /**
     * Quotes a CSV cell when it contains a comma, quote or line break. Quotes inside the cell are doubled.
     *
     * @param value the cell text, or null
     * @return the text ready to put in a CSV row, or an empty string for null
     */
    static String cell(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return '"' + value.replace("\"", "\"\"") + '"';
        }
        return value;
    }

    // ---- Plain text ----

    /**
     * Turns the report into plain text for reading or printing: numbered section headings, with each
     * table's columns lined up.
     *
     * @param report the report to convert
     * @return the text
     */
    public String toText(CustomReport report) {
        StringBuilder text = new StringBuilder();
        long days = ChronoUnit.DAYS.between(report.from(), report.to()) + 1;
        text.append(TITLE).append('\n').append("=".repeat(TITLE.length())).append("\n\n");
        text.append("Utility:   ").append(report.utilityLabel()).append('\n');
        text.append("Period:    ").append(report.from().format(DAY)).append(" – ").append(report.to().format(DAY))
                .append(" (").append(days).append(days == 1 ? " day" : " days").append(")\n");
        text.append("Generated: ").append(report.generatedOn().format(DAY)).append('\n');

        int number = 1;
        for (Section section : report.sections()) {
            String heading = number++ + ". " + section.title();
            text.append('\n').append(heading).append('\n').append("-".repeat(heading.length())).append('\n');
            for (Table table : section.tables()) {
                text.append('\n').append(table.heading()).append('\n');
                appendAligned(text, table);
            }
            if (!section.notes().isEmpty()) {
                text.append('\n');
                for (String note : section.notes()) {
                    text.append("* ").append(note).append('\n');
                }
            }
        }
        return text.toString();
    }

    /** Lines the columns up so the table is readable in any text editor. */
    private static void appendAligned(StringBuilder text, Table table) {
        int columns = table.columns().size();
        int[] widths = new int[columns];
        for (int c = 0; c < columns; c++) {
            widths[c] = table.columns().get(c).length();
            for (List<String> row : table.rows()) {
                if (c < row.size()) {
                    widths[c] = Math.max(widths[c], row.get(c).length());
                }
            }
        }
        appendLine(text, table.columns(), widths);
        int total = 0;
        for (int width : widths) {
            total += width + 2;
        }
        text.append("  ").append("-".repeat(Math.max(0, total - 2))).append('\n');
        for (List<String> row : table.rows()) {
            appendLine(text, row, widths);
        }
    }

    private static void appendLine(StringBuilder text, List<String> cells, int[] widths) {
        StringBuilder line = new StringBuilder("  ");
        for (int c = 0; c < widths.length; c++) {
            String value = c < cells.size() ? cells.get(c) : "";
            line.append(value).append(" ".repeat(widths[c] - value.length() + 2));
        }
        text.append(line.toString().stripTrailing()).append('\n');
    }

    // ---- Saving ----

    /** Writes to a temporary file first, then moves it into place, so a failure never leaves a partial file. */
    private static ExportResult writeSafely(Path file, String content) {
        Path target = file.toAbsolutePath();
        Path temp = null;
        try {
            temp = Files.createTempFile(target.getParent(), "report-", ".tmp");
            Files.writeString(temp, content, StandardCharsets.UTF_8);
            try {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, target, StandardCopyOption.REPLACE_EXISTING);
            }
            return new ExportResult(true, "Report saved to " + target);
        } catch (IOException e) {
            String reason = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
            return new ExportResult(false, "Could not write file: " + reason);
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
}
