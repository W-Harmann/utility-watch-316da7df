package com.utilitywatch.controller;

import com.utilitywatch.notifications.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class NotificationsEntryController {

    @FXML
    private Label notificationTitleLabel;
    @FXML
    private Label notificationTimeLabel;
    @FXML
    private Label notificationMessageLabel;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d yyyy h:mma");

    public void setNotification(Notification notification) {
        notificationTitleLabel.setText(notification.getTitle());
        notificationTimeLabel.setText(notification.getDateTime().format(FORMATTER));
        notificationMessageLabel.setText(notification.getMessage());
    }
}

