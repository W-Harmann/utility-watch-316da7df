package com.utilitywatch.reports;

/**
 * US7.3 - the pieces of information a user can choose to include in a custom report.
 * <p>The declaration order is the order the sections appear in the generated report, so the report
 * stays in the same layout whichever sections are picked.
 */
public enum ReportSection {
    /** Reading count, total, average, median, lowest, highest, standard deviation and variability. */
    CONSUMPTION_STATISTICS("Consumption Statistics"),
    /** Usage grouped by week or month, with an overall trend. */
    TRENDS_ANALYSIS("Trends Analysis"),
    /** The selected period compared with the previous period of the same length. */
    COMPARISONS("Comparisons"),
    /** Spikes, long periods of high use, and use outside active hours. */
    UNUSUAL_PATTERNS("Unusual Patterns"),
    /** Changes since the previous period, with suggestions for saving energy or water. */
    CONSERVATION_OPPORTUNITIES("Conservation Opportunities"),
    /** Estimated cost, using the rates set on the Costs page. */
    COST_SUMMARY("Cost Summary"),
    /** Carbon footprint, using the emission factors set on the Impact page. */
    ENVIRONMENTAL_IMPACT("Environmental Impact"),
    /** US11.1 / US11.2 - the raw readings that used to be exported from the Costs page. */
    READINGS_DATA("Readings Data");

    private final String title;

    ReportSection(String title) {
        this.title = title;
    }

    /**
     * Returns the name shown for this section.
     *
     * @return the display title, for example {@code "Cost Summary"}
     */
    public String getTitle() {
        return title;
    }
}
