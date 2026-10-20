package com.utilitywatch.controller;

import com.utilitywatch.notifications.Notification;
import com.utilitywatch.notifications.NotificationRepository;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class NotificationsEntryController {

    @FXML
    private Label notificationTitleLabel;
    @FXML
    private Label notificationTimeLabel;
    @FXML
    private Label notificationMessageLabel;
    @FXML
    private Button notificationIgnoreButton;
    @FXML
    private HBox notificationHBox;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("MMM d yyyy h:mma");

    private final NotificationRepository notificationRepository = NotificationRepository.getInstance();

    private Notification notification;

    private VBox notificationsContainer;

    private Label monthlyNotificationsLabel;
    private Label allNotificationsLabel;
    private Label ignoredNotificationsLabel;



    public void setNotification(Notification notification, VBox notificationsContainer, Label monthlyNotificationsLabel, Label allNotificationsLabel, Label ignoredNotificationsLabel) {
        notificationTitleLabel.setText(notification.getTitle());
        notificationTimeLabel.setText(notification.getDateTime().format(FORMATTER));
        notificationMessageLabel.setText(notification.getMessage());
        if (notification.getIgnoredStatus()) {
            notificationIgnoreButton.setText("Important");
        } else {
            notificationIgnoreButton.setText("Ignore");
        }
        this.notification = notification;
        this.notificationsContainer = notificationsContainer;
        this.monthlyNotificationsLabel = monthlyNotificationsLabel;
        this.allNotificationsLabel = allNotificationsLabel;
        this.ignoredNotificationsLabel = ignoredNotificationsLabel;
    }

    @FXML
    protected void onIgnore() {
        if (notificationsContainer != null) {
            notificationsContainer.getChildren().remove(notificationHBox);
        }
        notificationRepository.ignore(notification);

        if (notification.getIgnoredStatus()) {
            ignoredNotificationsLabel.setText(String.valueOf(Integer.parseInt( ignoredNotificationsLabel.getText()) - 1));
            allNotificationsLabel.setText(String.valueOf(Integer.parseInt( allNotificationsLabel.getText()) + 1));
            if (!notification.getDateTime().isBefore(LocalDateTime.now().minusMonths(1))) {
                monthlyNotificationsLabel.setText(String.valueOf(Integer.parseInt( monthlyNotificationsLabel.getText()) + 1));
            }
        }
        else {
            ignoredNotificationsLabel.setText(String.valueOf(Integer.parseInt( ignoredNotificationsLabel.getText()) + 1));
            allNotificationsLabel.setText(String.valueOf(Integer.parseInt( allNotificationsLabel.getText()) - 1));
            if (!notification.getDateTime().isBefore(LocalDateTime.now().minusMonths(1))) {
                monthlyNotificationsLabel.setText(String.valueOf(Integer.parseInt( monthlyNotificationsLabel.getText()) - 1));
            }
        }


    }
}

