package com.utilitywatch.reports;

// US7.3 - the pieces of information a user can choose to include in a custom report.
// The declaration order is the order the sections appear in the generated report,
// so the report stays in the same layout whichever sections are picked.
public enum ReportSection {
    CONSUMPTION_STATISTICS("Consumption Statistics"),
    TRENDS_ANALYSIS("Trends Analysis"),
    COMPARISONS("Comparisons"),
    UNUSUAL_PATTERNS("Unusual Patterns"),
    CONSERVATION_OPPORTUNITIES("Conservation Opportunities"),
    COST_SUMMARY("Cost Summary"),
    ENVIRONMENTAL_IMPACT("Environmental Impact"),
    // US11.1 / US11.2 - the raw readings that used to be exported from the Costs page
    READINGS_DATA("Readings Data");

    private final String title;

    ReportSection(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }
}
