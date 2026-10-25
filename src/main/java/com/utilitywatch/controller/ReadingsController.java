package com.utilitywatch.controller;

import com.utilitywatch.Reading;
import com.utilitywatch.database.CsvReadingImportService;
import com.utilitywatch.database.ReadingDAO;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;

import com.utilitywatch.notifications.Notification;
import com.utilitywatch.notifications.NotificationRepository;
import com.utilitywatch.notifications.Type;
import javafx.collections.FXCollections;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.HBox;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Controller for the Readings screen ({@code readings-view.fxml}). It lets the user add, edit and
 * delete readings and import them from a CSV file. Only the logged-in user's readings are shown.
 * <p>Each time the table is refreshed, the user's readings are also checked for unusual consumption
 * patterns, and notifications are added for any that are found.
 */
public class ReadingsController {

    @FXML private DatePicker datePicker;
    @FXML private ComboBox<String> resourceTypeBox;
    @FXML private TextField quantityField;
    @FXML private Button saveButton;
    @FXML private Label errorLabel;
    @FXML private Label importResultLabel;
    @FXML private TableView<Reading> readingsTable;
    @FXML private TableColumn<Reading, String> dateColumn;
    @FXML private TableColumn<Reading, Double> quantityColumn;
    @FXML private TableColumn<Reading, String> resourceTypeColumn;
    @FXML private TableColumn<Reading, String> unitColumn;
    @FXML private TableColumn<Reading, Void> actionsColumn;

    private final ReadingDAO readingDAO = SharedReadingDAO.getDAO();
    private final CsvReadingImportService csvImportService = new CsvReadingImportService(readingDAO);
    private int currentUserId;
    private Reading readingBeingEdited = null; // null means the form is adding a new reading, not editing one

    private final NotificationRepository notificationRepository = NotificationRepository.getInstance();

    /**
     * Sets up the screen: finds the logged-in user, fills the resource type list, sets up the table
     * columns and loads the user's readings. Called by the FXML loader.
     */
    @FXML
    public void initialize() {
        User currentUser = Session.getInstance().getCurrentUser();
        currentUserId = currentUser.getId();

        resourceTypeBox.setItems(FXCollections.observableArrayList("energy", "water"));

        dateColumn.setCellValueFactory(new PropertyValueFactory<>("dateTime"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        resourceTypeColumn.setCellValueFactory(new PropertyValueFactory<>("resourceType"));
        unitColumn.setCellValueFactory(new PropertyValueFactory<>("unit"));
        setUpActionsColumn();
        refreshTable();
    }

    // builds the Edit/Delete buttons shown in each row of the table
    private void setUpActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<Reading, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(e -> {
                    // load this row's values into the form and switch it into edit mode
                    Reading reading = getTableView().getItems().get(getIndex());
                    readingBeingEdited = reading;
                    datePicker.setValue(reading.getDateTime().toLocalDate());
                    resourceTypeBox.setValue(reading.getResourceType());
                    quantityField.setText(String.valueOf(reading.getQuantity()));
                    saveButton.setText("Update Reading");
                });
                deleteButton.setOnAction(e -> {
                    Reading reading = getTableView().getItems().get(getIndex());
                    readingDAO.deleteReading(reading.getId());
                    refreshTable();
                });
            }

            // JavaFX calls this for every row - only show the buttons on real rows, not empty ones
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : buttons);
            }
        });
    }

    @FXML
    private void onSaveClick() {
        try {
            if (datePicker.getValue() == null) {
                throw new IllegalArgumentException("Date is required");
            }
            if (resourceTypeBox.getValue() == null) {
                throw new IllegalArgumentException("Type is required");
            }
            double quantity = Double.parseDouble(quantityField.getText());

            if (readingBeingEdited == null) {
                // adding a brand new reading
                Reading reading = new Reading(datePicker.getValue().atStartOfDay(), quantity);
                reading.setResourceType(resourceTypeBox.getValue());
                reading.setUserId(currentUserId);
                readingDAO.insert(reading);
            } else {
                // updating the reading the user clicked Edit on
                readingBeingEdited.setDate(datePicker.getValue().atStartOfDay());
                readingBeingEdited.setResourceType(resourceTypeBox.getValue());
                readingBeingEdited.setQuantity(quantity);
                readingDAO.updateReading(readingBeingEdited);
                readingBeingEdited = null;
                saveButton.setText("Save Reading");
            }

            errorLabel.setText("");
            quantityField.clear();
            datePicker.setValue(null);
            resourceTypeBox.setValue(null);
            refreshTable();
        } catch (NumberFormatException e) {
            errorLabel.setText("Quantity must be a number");
        } catch (IllegalArgumentException e) {
            errorLabel.setText(e.getMessage());
        }
    }

    @FXML
    private void onImportClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        Stage stage = (Stage) quantityField.getScene().getWindow();
        File file = fileChooser.showOpenDialog(stage);
        if (file == null) {
            return;
        }

        try {
            String content = Files.readString(file.toPath());
            CsvReadingImportService.ImportReport report = csvImportService.importCsv(content, currentUserId);
            importResultLabel.setText(report.getSuccessCount() + " imported, "
                    + report.getFailureMessages().size() + " skipped");
            refreshTable();
            // clears unusual Notifications when data is completely changed
            notificationRepository.clearUnusual();
        } catch (IOException e) {
            importResultLabel.setText("Could not read file");
        }
    }

    private void refreshTable() {
        readingsTable.setItems(FXCollections.observableArrayList(readingDAO.getReadingsForUser(currentUserId)));
        refreshUnusualPatterns();
    }

    // So that Notifications for unusual patterns are set upon data being changed
    private void refreshUnusualPatterns() {
        LinkedHashMap<List<Reading>, String> flaggedReadingsMap = ReportsController.loadUnusualConsumptionData(readingDAO.getReadingsForUser(currentUserId));

        if (!flaggedReadingsMap.isEmpty()) {

            for (Map.Entry<List<Reading>, String> flaggedReadingEntry : flaggedReadingsMap.entrySet()) {
                notifyUnusualPattern(flaggedReadingEntry);
            }
        }


    }

    private void notifyUnusualPattern(Map.Entry<List<Reading>, String> flaggedReadingEntry) {
        List<Reading> flaggedReadings = flaggedReadingEntry.getKey();
        String flaggedReason = flaggedReadingEntry.getValue();

        double readingDuration = Duration.between(flaggedReadings.getFirst().getDateTime(), flaggedReadings.getLast().getDateTime()).toHours();

        double totalUsage = 0;

        for (Reading reading : flaggedReadings) {
            totalUsage += reading.getQuantity();
        }

        String formattedTotal = String.format("%.1f", totalUsage);

        String formattedResourceType = flaggedReadings.getFirst().getResourceType().substring(0, 1).toUpperCase() + flaggedReadings.getFirst().getResourceType().substring(1);

        LocalDateTime dateTime = flaggedReadings.getFirst().getDateTime();

        String title = "Unusual Pattern Detected: " + formattedResourceType + " Usage";

        String message = formattedResourceType + " " + flaggedReason.toLowerCase() + " of " + formattedTotal + " "
                + flaggedReadings.getFirst().getUnit() + (readingDuration == 0 ? "" : " over " + readingDuration + " hours");

        if (!unusualNotificationChecker(dateTime, title, message)) {
            notificationRepository.add(new Notification(
                    dateTime,
                    title,
                    message,
                    false,
                    Type.UNUSUALPATTERN));
        }


    }

    private boolean unusualNotificationChecker(LocalDateTime dateTime, String title, String message) {

        List<Notification> notifications = notificationRepository.getAll();

        for (Notification notification : notifications) {
            if (Objects.equals(notification.getDateTime(), dateTime) && Objects.equals(notification.getTitle(), title) && Objects.equals(notification.getMessage(), message)) {
                return true;
            }
        }

        return false;

    }
}