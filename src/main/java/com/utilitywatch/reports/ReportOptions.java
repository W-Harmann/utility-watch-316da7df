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
 */
public record ReportOptions(String utility, LocalDate from, LocalDate to, Set<ReportSection> sections) {

    public ReportOptions {
        // defensive, ordered copy so the report always lists sections in the same order
        sections = sections == null || sections.isEmpty()
                ? Collections.unmodifiableSet(EnumSet.noneOf(ReportSection.class))
                : Collections.unmodifiableSet(EnumSet.copyOf(sections));
    }

    /** Returns a message explaining what is wrong, or empty if a report can be generated. */
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

    public boolean isValid() {
        return validate().isEmpty();
    }

    public boolean includes(ReportSection section) {
        return sections.contains(section);
    }

    /** Number of whole days in the range, counting both ends. */
    public long days() {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }
}
