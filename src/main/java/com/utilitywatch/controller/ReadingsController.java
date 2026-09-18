package com.utilitywatch.controller;

import com.utilitywatch.Reading;
import com.utilitywatch.database.CsvReadingImportService;
import com.utilitywatch.database.ReadingDAO;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;

import javafx.collections.FXCollections;
import javafx.scene.layout.HBox;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

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
    private Reading readingBeingEdited = null;

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

    private void setUpActionsColumn() {
        actionsColumn.setCellFactory(col -> new TableCell<Reading, Void>() {
            private final Button editButton = new Button("Edit");
            private final Button deleteButton = new Button("Delete");
            private final HBox buttons = new HBox(5, editButton, deleteButton);

            {
                editButton.setOnAction(e -> {
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
                Reading reading = new Reading(datePicker.getValue().atStartOfDay(), quantity);
                reading.setResourceType(resourceTypeBox.getValue());
                reading.setUserId(currentUserId);
                readingDAO.insert(reading);
            } else {
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
        } catch (IOException e) {
            importResultLabel.setText("Could not read file");
        }
    }

    private void refreshTable() {
        readingsTable.setItems(FXCollections.observableArrayList(readingDAO.getReadingsForUser(currentUserId)));
    }
}