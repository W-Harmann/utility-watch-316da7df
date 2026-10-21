package com.utilitywatch.controller;

import com.utilitywatch.database.IReadingDAO;
import com.utilitywatch.login.AuthenticationService;
import com.utilitywatch.login.Session;
import com.utilitywatch.login.User;

import com.utilitywatch.notifications.NotificationRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Optional;

// Settings page: edit profile details (US13.4) and delete the account (US12.3)
public class SettingsController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private TextField addressField;
    @FXML private Label errorLabel;
    @FXML private Label savedLabel;

    private final AuthenticationService authService = SharedAuth.getService();
    private final IReadingDAO readingDAO = SharedReadingDAO.getDAO();

    // pre-fills the fields with the logged-in user's current details
    @FXML
    public void initialize() {
        showCurrentDetails();
    }

    private void showCurrentDetails() {
        User user = Session.getInstance().getCurrentUser();
        nameField.setText(user.getName() == null ? "" : user.getName());
        emailField.setText(user.getEmail());
        addressField.setText(user.getPropertyAddress() == null ? "" : user.getPropertyAddress());
    }

    @FXML
    private void onSaveClick() {
        errorLabel.setText("");
        savedLabel.setText("");
        try {
            authService.updateProfile(Session.getInstance().getCurrentUser(),
                    nameField.getText(), emailField.getText(), addressField.getText());
            savedLabel.setText("Changes saved");
        } catch (IllegalArgumentException e) {
            // refused (e.g. a required field was cleared): the previous details are kept and shown again
            errorLabel.setText(e.getMessage());
        } catch (RuntimeException e) {
            errorLabel.setText("Couldn't save your changes");
        }
        showCurrentDetails();
    }

    @FXML
    private void onDeleteClick() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "This permanently deletes your account and all of your readings. This can't be undone.",
                ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Delete account");
        confirm.setHeaderText("Delete your account?");
        Optional<ButtonType> answer = confirm.showAndWait();
        if (answer.isEmpty() || answer.get() != ButtonType.YES) {
            return;
        }

        User user = Session.getInstance().getCurrentUser();
        try {
            readingDAO.deleteReadingsForUser(user.getId());
            authService.deleteAccount(user);
        } catch (RuntimeException e) {
            errorLabel.setText("Couldn't delete your account");
            return;
        }

        // goals, notification, cost settings and the date range are shared in-memory data,
        // so clear them too or the next person to log in would see them
        GoalsController.clearGoals();
        NotificationRepository.getInstance().clear();
        SharedUtilitySettings.reset();
        SharedDateRange.clear();

        try {
            Stage stage = (Stage) nameField.getScene().getWindow();
            Parent root = FXMLLoader.load(getClass().getResource("/com/utilitywatch/login-view.fxml"));
            stage.setScene(new Scene(root, 400, 400));
        } catch (IOException e) {
            errorLabel.setText("Account deleted, but couldn't load the login screen");
        }
    }
}