package com.utilitywatch.reports;

import java.time.LocalDate;
import java.util.List;

/**
 * US7.3 - a generated report. It holds only the sections the user selected, each made of
 * simple tables and notes, so it can be shown on screen or written to a file in the same layout.
 *
 * @param utilityLabel {@code "Energy"}, {@code "Water"} or {@code "Energy and Water"}
 * @param from the first day covered (inclusive)
 * @param to the last day covered (inclusive)
 * @param generatedOn the day the report was generated
 * @param hasReadings whether the chosen utilities have any readings in the period
 * @param sections the selected sections, in report order
 */
public record CustomReport(String utilityLabel, LocalDate from, LocalDate to, LocalDate generatedOn,
                           boolean hasReadings, List<Section> sections) {

    /** Makes an unmodifiable copy of the section list, so the report cannot be changed once built. */
    public CustomReport {
        sections = List.copyOf(sections);
    }

    /**
     * One selected section, e.g. "Cost Summary".
     *
     * @param type which kind of section this is
     * @param tables the tables in the section
     * @param notes short notes shown under the tables, for example "No energy readings in this period."
     */
    public record Section(ReportSection type, List<Table> tables, List<String> notes) {
        /** Makes unmodifiable copies of the tables and notes. */
        public Section {
            tables = List.copyOf(tables);
            notes = List.copyOf(notes);
        }

        /**
         * Returns the section's heading.
         *
         * @return the display title of the section's type
         */
        public String title() {
            return type.getTitle();
        }
    }

    /**
     * A small table with a heading (e.g. "Energy"), column names and rows of text cells.
     *
     * @param heading the table's heading
     * @param columns the column names
     * @param rows the rows, each a list of text cells
     */
    public record Table(String heading, List<String> columns, List<List<String>> rows) {
        /** Makes unmodifiable copies of the column names and rows. */
        public Table {
            columns = List.copyOf(columns);
            rows = rows.stream().map(List::copyOf).toList();
        }
    }

    /**
     * Lists which kinds of section the report contains.
     *
     * @return the type of each section, in report order
     */
    public List<ReportSection> sectionTypes() {
        return sections.stream().map(Section::type).toList();
    }
}
