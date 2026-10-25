package com.utilitywatch.reports;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

import static com.utilitywatch.ReadingEvaluator.BOTH;
import static com.utilitywatch.ReadingEvaluator.ENERGY;
import static com.utilitywatch.ReadingEvaluator.WATER;

/**
 * US7.3 / US11.2 - what the user picked on the Customise Report screen:
 * the utility ("energy", "water" or "both"), a custom date range, and the sections to include.
 *
 * @param utility {@code "energy"}, {@code "water"} or {@code "both"}
 * @param from the first day of the report (inclusive)
 * @param to the last day of the report (inclusive)
 * @param sections the sections to include; stored as an unmodifiable set in report order
 */
public record ReportOptions(String utility, LocalDate from, LocalDate to, Set<ReportSection> sections) {

    /**
     * Copies the sections into an unmodifiable set in report order. A null or empty set becomes an empty set.
     */
    public ReportOptions {
        // defensive, ordered copy so the report always lists sections in the same order
        sections = sections == null || sections.isEmpty()
                ? Collections.unmodifiableSet(EnumSet.noneOf(ReportSection.class))
                : Collections.unmodifiableSet(EnumSet.copyOf(sections));
    }

    /**
     * Checks the options. The checks run in this order and the first problem found is returned: no
     * sections selected, an unknown utility, a missing date, then an end date before the start date.
     *
     * @return a message explaining what is wrong, or empty if a report can be generated
     */
    public Optional<String> validate() {
        if (sections.isEmpty()) {
            return Optional.of("Select at least one item to include in your report");
        }
        if (!ENERGY.equals(utility) && !WATER.equals(utility) && !BOTH.equals(utility)) {
            return Optional.of("Choose energy, water or both");
        }
        if (from == null || to == null) {
            return Optional.of("Choose both a start and end date");
        }
        if (to.isBefore(from)) {
            return Optional.of("The end date must be on or after the start date");
        }
        return Optional.empty();
    }

    /**
     * Says whether a report can be generated from these options.
     *
     * @return {@code true} if {@link #validate()} finds no problem
     */
    public boolean isValid() {
        return validate().isEmpty();
    }

    /**
     * Says whether a section was selected.
     *
     * @param section the section to look for
     * @return {@code true} if it is one of the selected sections
     */
    public boolean includes(ReportSection section) {
        return sections.contains(section);
    }

    /**
     * Counts the days in the range, including both the first and last day. Only call this once
     * {@link #isValid()} is true, as it needs both dates.
     *
     * @return the number of whole days in the range
     */
    public long days() {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }
}
