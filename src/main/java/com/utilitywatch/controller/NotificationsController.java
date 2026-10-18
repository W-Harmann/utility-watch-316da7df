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
    @FXML
    private VBox recentNotificationsPage;

    @FXML
    private VBox ignoredNotificationsContainer;
    @FXML
    private VBox ignoredNotificationsPage;
    @FXML
    private Label ignoredNotificationsLabel;


    private final NotificationRepository notificationRepository = NotificationRepository.getInstance();

    // adds completed goal notifications to the top of the recent notifications list (us8.4)
    @FXML
    public void initialize() {
        recentNotificationsPage.setVisible(true);
        recentNotificationsPage.setManaged(true);

        updateNotificationsContainer();
    }

    // returns each notification as a node so it can be displayed as an individual entry in the notifications list
    private Node createNotificationEntryNode(Notification notification, VBox notificationsContainer, Label ignoredNotificationsLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/notification-entry.fxml"));
            Node notificationEntry = loader.load();
            NotificationsEntryController controller = loader.getController();
            controller.setNotification(notification, notificationsContainer, ignoredNotificationsLabel);
            return notificationEntry;
        } catch (IOException e) {
            return new Label("Failed to load notification");
        }
    }

    private void updateNotificationsContainer() {

        int ignoreCount = 0;

        for (Notification notification : notificationRepository.getAll()) {
            // newest notifications are the first in the list
            if (!notification.getIgnoredStatus()) {
                recentNotificationsContainer.getChildren().addLast(createNotificationEntryNode(notification, recentNotificationsContainer, ignoredNotificationsLabel));
            }
            else {
                ignoredNotificationsContainer.getChildren().addLast(createNotificationEntryNode(notification, ignoredNotificationsContainer, ignoredNotificationsLabel));
                ignoreCount++;
            }
        }

        ignoredNotificationsLabel.setText(String.valueOf(ignoreCount));
    }

    private void hideNotificationContainers() {
        recentNotificationsPage.setVisible(false);
        recentNotificationsPage.setManaged(false);

        ignoredNotificationsPage.setVisible(false);
        ignoredNotificationsPage.setManaged(false);
    }

    @FXML
    protected void onViewIgnoredNotifications() {
        hideNotificationContainers();

        ignoredNotificationsPage.setVisible(true);
        ignoredNotificationsPage.setManaged(true);

        ignoredNotificationsContainer.getChildren().clear();

        updateNotificationsContainer();
    }

    @FXML
    protected void onViewAllNotifications() {
        hideNotificationContainers();

        recentNotificationsPage.setVisible(true);
        recentNotificationsPage.setManaged(true);

        recentNotificationsContainer.getChildren().clear();

        updateNotificationsContainer();
    }
}
