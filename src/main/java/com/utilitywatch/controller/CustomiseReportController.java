package com.utilitywatch.controller;

import com.utilitywatch.Exporter.ExportResult;
import com.utilitywatch.Reading;
import com.utilitywatch.login.Session;
import com.utilitywatch.reports.CustomReport;
import com.utilitywatch.reports.CustomReportBuilder;
import com.utilitywatch.reports.CustomReportWriter;
import com.utilitywatch.reports.ReportOptions;
import com.utilitywatch.reports.ReportSection;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.utilitywatch.ReadingEvaluator.BOTH;

// US7.3 - customise which information goes into a consumption report.
// US11.2 - pick a custom date range (or a quick range like one billing cycle or one year) before exporting.
// Opened from the home screen's Export Data tile; it replaces the old export on the Costs page.
public class CustomiseReportController {

    private static final DateTimeFormatter DAY = DateTimeFormatter.ofPattern("d MMM yyyy");

    /** Long tables (e.g. readings data) are cut short in the on-screen preview; the saved file has every row. */
    private static final int PREVIEW_ROW_LIMIT = 40;

    @FXML private ToggleGroup customiseUtilityToggleGroup;
    @FXML private ToggleButton energyToggle;
    @FXML private ToggleButton waterToggle;
    @FXML private ToggleButton bothToggle;
    @FXML private PeriodPickerController periodPickerController;
    @FXML private Button allReadingsButton;
    @FXML private Label periodSummaryLabel;

    @FXML private VBox previewContainer;
    @FXML private CheckBox selectAllCheckBox;
    @FXML private CheckBox consumptionStatisticsCheckBox;
    @FXML private CheckBox trendsAnalysisCheckBox;
    @FXML private CheckBox comparisonsCheckBox;
    @FXML private CheckBox unusualPatternsCheckBox;
    @FXML private CheckBox conservationOpportunitiesCheckBox;
    @FXML private CheckBox costSummaryCheckBox;
    @FXML private CheckBox environmentalImpactCheckBox;
    @FXML private CheckBox readingsDataCheckBox;
    @FXML private Label selectionNoteLabel;

    @FXML private Label statusLabel;
    @FXML private Button generateReportButton;

    private final CustomReportBuilder reportBuilder = new CustomReportBuilder();
    private final CustomReportWriter reportWriter = new CustomReportWriter();
    private final Map<ReportSection, CheckBox> sectionCheckBoxes = new EnumMap<>(ReportSection.class);
    private List<Reading> readings = List.of();

    // reference back to MainController to navigate to other screens
    private MainController mainController;

    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    @FXML
    public void initialize() {
        int userId = Session.getInstance().getCurrentUser().getId();
        readings = SharedReadingDAO.getDAO().getReadingsForUser(userId);

        sectionCheckBoxes.put(ReportSection.CONSUMPTION_STATISTICS, consumptionStatisticsCheckBox);
        sectionCheckBoxes.put(ReportSection.TRENDS_ANALYSIS, trendsAnalysisCheckBox);
        sectionCheckBoxes.put(ReportSection.COMPARISONS, comparisonsCheckBox);
        sectionCheckBoxes.put(ReportSection.UNUSUAL_PATTERNS, unusualPatternsCheckBox);
        sectionCheckBoxes.put(ReportSection.CONSERVATION_OPPORTUNITIES, conservationOpportunitiesCheckBox);
        sectionCheckBoxes.put(ReportSection.COST_SUMMARY, costSummaryCheckBox);
        sectionCheckBoxes.put(ReportSection.ENVIRONMENTAL_IMPACT, environmentalImpactCheckBox);
        sectionCheckBoxes.put(ReportSection.READINGS_DATA, readingsDataCheckBox);

        // like the other pages' segmented control, one utility is always selected
        customiseUtilityToggleGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                customiseUtilityToggleGroup.selectToggle(oldToggle);
            } else {
                refreshPreview();
            }
        });

        periodPickerController.setOnPeriodChanged(this::refreshPreview);
        allReadingsButton.setDisable(readings.isEmpty());
        refreshPreview();
    }

    // ---- Quick date ranges (US11.2) ----

    @FXML
    private void onThisMonthClick() {
        LocalDate today = LocalDate.now();
        applyPeriod(today.withDayOfMonth(1), today);
    }

    @FXML
    private void onLastMonthClick() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);
        applyPeriod(lastMonth.atDay(1), lastMonth.atEndOfMonth());
    }

    // a typical quarterly billing cycle
    @FXML
    private void onLastThreeMonthsClick() {
        LocalDate today = LocalDate.now();
        applyPeriod(today.minusMonths(3).plusDays(1), today);
    }

    @FXML
    private void onLastYearClick() {
        LocalDate today = LocalDate.now();
        applyPeriod(today.minusYears(1).plusDays(1), today);
    }

    @FXML
    private void onAllReadingsClick() {
        Optional<LocalDate> first = readings.stream().map(r -> r.getDateTime().toLocalDate()).min(LocalDate::compareTo);
        Optional<LocalDate> last = readings.stream().map(r -> r.getDateTime().toLocalDate()).max(LocalDate::compareTo);
        if (first.isPresent()) {
            applyPeriod(first.get(), last.get());
        }
    }

    private void applyPeriod(LocalDate from, LocalDate to) {
        periodPickerController.setPeriod(from, to);
        refreshPreview();
    }

    // ---- Checkboxes (US7.3) ----

    // called by any option checkbox when it is clicked
    @FXML
    private void handleCheckboxAction(ActionEvent checkboxClickEvent) {
        refreshPreview();
    }

    // checks or unchecks all customise report options together
    @FXML
    private void onSelectAllClick() {
        boolean shouldSelectAll = selectAllCheckBox.isSelected();
        for (CheckBox optionCheckbox : sectionCheckBoxes.values()) {
            optionCheckbox.setSelected(shouldSelectAll);
        }
        refreshPreview();
    }

    private Set<ReportSection> selectedSections() {
        Set<ReportSection> selected = EnumSet.noneOf(ReportSection.class);
        sectionCheckBoxes.forEach((section, checkBox) -> {
            if (checkBox.isSelected()) {
                selected.add(section);
            }
        });
        return selected;
    }

    private ReportOptions currentOptions() {
        Toggle selected = customiseUtilityToggleGroup.getSelectedToggle();
        String utility = selected == null ? BOTH : (String) selected.getUserData();
        return new ReportOptions(utility, periodPickerController.getStart(), periodPickerController.getEnd(),
                selectedSections());
    }

    // ---- Preview ----

    // rebuilds the preview so it shows exactly what will be saved, and blocks generating an empty report
    private void refreshPreview() {
        Set<ReportSection> selected = selectedSections();
        selectAllCheckBox.setSelected(selected.size() == sectionCheckBoxes.size());
        setStatus("", false);

        ReportOptions options = currentOptions();
        updatePeriodSummary(options);

        boolean nothingSelected = selected.isEmpty();
        selectionNoteLabel.getStyleClass().remove("warning-note");
        if (nothingSelected) {
            selectionNoteLabel.getStyleClass().add("warning-note");
        }

        Optional<String> problem = options.validate();
        generateReportButton.setDisable(problem.isPresent());
        previewContainer.getChildren().clear();
        if (problem.isPresent()) {
            String message = nothingSelected ? "Select at least one item to see a preview of your report." : problem.get();
            previewContainer.getChildren().add(wrapped(message, "report-note"));
            return;
        }

        CustomReport report = reportBuilder.build(readings, options, currentRates());
        renderPreview(report);
    }

    private void updatePeriodSummary(ReportOptions options) {
        if (options.from() == null || options.to() == null) {
            periodSummaryLabel.setText("");
            return;
        }
        long days = ChronoUnit.DAYS.between(options.from(), options.to()) + 1;
        periodSummaryLabel.setText(days + (days == 1 ? " day selected" : " days selected"));
    }

    private void renderPreview(CustomReport report) {
        Label title = new Label("UtilityWatch Consumption Report");
        title.getStyleClass().add("report-info-box-title");
        previewContainer.getChildren().addAll(title,
                wrapped(report.utilityLabel() + " • " + report.from().format(DAY) + " – " + report.to().format(DAY),
                        "report-note"));

        if (!report.hasReadings()) {
            previewContainer.getChildren().add(wrapped(
                    "There are no readings for this utility in the selected period. Try a different range.", "warning-note"));
        }

        int number = 1;
        for (CustomReport.Section section : report.sections()) {
            Label heading = new Label(number++ + ". " + section.title());
            heading.getStyleClass().add("preview-section-title");
            previewContainer.getChildren().add(heading);

            for (CustomReport.Table table : section.tables()) {
                Label tableHeading = new Label(table.heading());
                tableHeading.getStyleClass().add(headingStyle(table.heading()));
                previewContainer.getChildren().addAll(tableHeading, grid(table));
                if (table.rows().size() > PREVIEW_ROW_LIMIT) {
                    previewContainer.getChildren().add(wrapped("… " + (table.rows().size() - PREVIEW_ROW_LIMIT)
                            + " more rows in the saved report", "report-note"));
                }
            }
            for (String note : section.notes()) {
                previewContainer.getChildren().add(wrapped(note, "report-note"));
            }
        }
    }

    private static GridPane grid(CustomReport.Table table) {
        GridPane grid = new GridPane();
        grid.setHgap(18);
        grid.setVgap(3);
        for (int column = 0; column < table.columns().size(); column++) {
            Label header = new Label(table.columns().get(column));
            header.getStyleClass().add("preview-column-header");
            grid.add(header, column, 0);
        }
        int rowsShown = Math.min(table.rows().size(), PREVIEW_ROW_LIMIT);
        for (int row = 0; row < rowsShown; row++) {
            List<String> cells = table.rows().get(row);
            for (int column = 0; column < cells.size(); column++) {
                Label cell = new Label(cells.get(column));
                cell.setWrapText(true);
                grid.add(cell, column, row + 1);
            }
        }
        return grid;
    }

    private static String headingStyle(String heading) {
        if (heading.startsWith("Energy")) {
            return "energy-column-header";
        }
        if (heading.startsWith("Water")) {
            return "water-column-header";
        }
        return "report-info-box-subtitle";
    }

    private static Label wrapped(String text, String styleClass) {
        Label label = new Label(text);
        label.setWrapText(true);
        label.getStyleClass().add(styleClass);
        return label;
    }

    private static CustomReportBuilder.Rates currentRates() {
        return new CustomReportBuilder.Rates(
                SharedUtilitySettings.getEnergyRate(), SharedUtilitySettings.getWaterRate(),
                SharedUtilitySettings.getEnergyEmission(), SharedUtilitySettings.getWaterEmission());
    }

    // ---- Buttons ----

    // sends the user back to the home screen
    @FXML
    protected void onCancelClick() {
        if (mainController != null) {
            mainController.showHome();
        }
    }

    // checks the selection, then saves the report as CSV or text
    @FXML
    protected void onGenerateReportClick() {
        ReportOptions options = currentOptions();
        Optional<String> problem = options.validate();
        if (problem.isPresent()) {
            setStatus(problem.get(), true);
            return;
        }
        CustomReport report = reportBuilder.build(readings, options, currentRates());
        if (!report.hasReadings()) {
            setStatus("No readings in the selected period, so there is nothing to export", true);
            return;
        }

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Save consumption report");
        FileChooser.ExtensionFilter csv = new FileChooser.ExtensionFilter("CSV spreadsheet (*.csv)", "*.csv");
        FileChooser.ExtensionFilter text = new FileChooser.ExtensionFilter("Text document (*.txt)", "*.txt");
        chooser.getExtensionFilters().addAll(csv, text);
        chooser.setSelectedExtensionFilter(csv);
        chooser.setInitialFileName("utilitywatch-report-" + options.utility() + "-" + options.from()
                + "-to-" + options.to() + ".csv");
        File file = chooser.showSaveDialog(generateReportButton.getScene().getWindow());
        if (file == null) {
            return; // user cancelled
        }
        file = withExtension(file, chooser.getSelectedExtensionFilter() == text ? ".txt" : ".csv");

        ExportResult result = reportWriter.write(report, file.toPath());
        setStatus(result.message(), !result.success());
    }

    // some platforms don't add the extension of the chosen file type, so add it unless the user typed one
    private static File withExtension(File file, String extension) {
        String name = file.getName().toLowerCase(Locale.ROOT);
        if (name.endsWith(".csv") || name.endsWith(".txt")) {
            return file;
        }
        return new File(file.getParentFile(), file.getName() + extension);
    }

    private void setStatus(String message, boolean isError) {
        statusLabel.setText(message);
        statusLabel.getStyleClass().removeAll("error-label", "success-label");
        if (!message.isEmpty()) {
            statusLabel.getStyleClass().add(isError ? "error-label" : "success-label");
        }
    }
}
