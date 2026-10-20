package com.utilitywatch.reports;

import java.time.LocalDate;
import java.util.List;

/**
 * US7.3 - a generated report. It holds only the sections the user selected, each made of
 * simple tables and notes, so it can be shown on screen or written to a file in the same layout.
 */
public record CustomReport(String utilityLabel, LocalDate from, LocalDate to, LocalDate generatedOn,
                           boolean hasReadings, List<Section> sections) {

    public CustomReport {
        sections = List.copyOf(sections);
    }

    /** One selected section, e.g. "Cost Summary". */
    public record Section(ReportSection type, List<Table> tables, List<String> notes) {
        public Section {
            tables = List.copyOf(tables);
            notes = List.copyOf(notes);
        }

        public String title() {
            return type.getTitle();
        }
    }

    /** A small table with a heading (e.g. "Energy"), column names and rows of text cells. */
    public record Table(String heading, List<String> columns, List<List<String>> rows) {
        public Table {
            columns = List.copyOf(columns);
            rows = rows.stream().map(List::copyOf).toList();
        }
    }

    public List<ReportSection> sectionTypes() {
        return sections.stream().map(Section::type).toList();
    }
}
