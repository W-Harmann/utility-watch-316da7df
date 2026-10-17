package com.utilitywatch.controller;

import com.utilitywatch.notifications.Notification;
import com.utilitywatch.notifications.NotificationRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;

public class NotificationsController {


    @FXML
    private VBox recentNotificationsContainer;

    private final NotificationRepository notificationRepository = NotificationRepository.getInstance();

    // adds completed goal notifications to the top of the recent notifications list (us8.4)
    @FXML
    public void initialize() {
        for (Notification notification : notificationRepository.getAll()) {
            // index 0 to put the newest notification at the top
            recentNotificationsContainer.getChildren().add(0, createNotificationEntryNode(notification));
        }

    }

    // returns each notification as a node so it can be displayed as an individual entry in the notifications list
    private Node createNotificationEntryNode(Notification notification) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/notification-entry.fxml"));
            Node notificationEntry = loader.load();
            NotificationsEntryController controller = loader.getController();
            controller.setNotification(notification);
            return notificationEntry;
        } catch (IOException e) {
            return new Label("Failed to load notification");
        }
    }

    @FXML
    protected void onViewAllNotifications() {
    }
}
