package com.utilitywatch.controller;

import com.utilitywatch.notifications.Notification;
import com.utilitywatch.notifications.NotificationRepository;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.io.IOException;
import java.time.LocalDateTime;

/**
 * Controller for the Notifications screen ({@code notifications-view.fxml}). Allows the user to view recent, all and
 * ignored notifications. Notifications can be ignored removing notification from the recent and all views.
 */
public class NotificationsController {



    @FXML
    private  VBox monthlyNotificationsContainer;
    @FXML
    private VBox monthlyNotificationsPage;
    @FXML
    private Label monthlyNotificationsLabel;

    @FXML
    private VBox allNotificationsContainer;
    @FXML
    private VBox allNotificationsPage;
    @FXML
    private Label allNotificationsLabel;

    @FXML
    private VBox ignoredNotificationsContainer;
    @FXML
    private VBox ignoredNotificationsPage;
    @FXML
    private Label ignoredNotificationsLabel;


    private final NotificationRepository notificationRepository = NotificationRepository.getInstance();

    /**
     * Opens on the recent notifications page and fills the notification lists, with completed goal
     * notifications at the top (US8.4). Called by the FXML loader.
     */
    @FXML
    public void initialize() {
        monthlyNotificationsPage.setVisible(true);
        monthlyNotificationsPage.setManaged(true);

        updateNotificationsContainer();
    }

    // returns each notification as a node so it can be displayed as an individual entry in the notifications list
    private Node createNotificationEntryNode(Notification notification, VBox notificationsContainer, Label monthlyNotificationsLabel, Label allNotificationsLabel,  Label ignoredNotificationsLabel) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/utilitywatch/notification-entry.fxml"));
            Node notificationEntry = loader.load();
            NotificationsEntryController controller = loader.getController();
            controller.setNotification(notification, notificationsContainer, monthlyNotificationsLabel, allNotificationsLabel, ignoredNotificationsLabel);
            return notificationEntry;
        } catch (IOException e) {
            return new Label("Failed to load notification");
        }
    }

    private void updateNotificationsContainer() {

        int monthlyCount = 0;
        int allCount = 0;
        int ignoreCount = 0;

        for (Notification notification : notificationRepository.getAll()) {
            // newest notifications are the first in the list
            if (!notification.getIgnoredStatus()) {
                allNotificationsContainer.getChildren().addLast(createNotificationEntryNode(notification, allNotificationsContainer, monthlyNotificationsLabel, allNotificationsLabel, ignoredNotificationsLabel));
                allCount++;
                if (!notification.getDateTime().isBefore(LocalDateTime.now().minusMonths(1))) {
                    monthlyNotificationsContainer.getChildren().addLast(createNotificationEntryNode(notification, monthlyNotificationsContainer, monthlyNotificationsLabel, allNotificationsLabel, ignoredNotificationsLabel));
                    monthlyCount++;
                }
            }
            else {
                ignoredNotificationsContainer.getChildren().addLast(createNotificationEntryNode(notification, ignoredNotificationsContainer, monthlyNotificationsLabel, allNotificationsLabel, ignoredNotificationsLabel));
                ignoreCount++;
            }
        }

        monthlyNotificationsLabel.setText(String.valueOf(monthlyCount));
        allNotificationsLabel.setText(String.valueOf(allCount));
        ignoredNotificationsLabel.setText(String.valueOf(ignoreCount));
    }

    private void hideNotificationContainers() {
        monthlyNotificationsPage.setVisible(false);
        monthlyNotificationsPage.setManaged(false);

        allNotificationsPage.setVisible(false);
        allNotificationsPage.setManaged(false);

        ignoredNotificationsPage.setVisible(false);
        ignoredNotificationsPage.setManaged(false);
    }

    @FXML
    protected void onViewMonthlyNotifications() {
        hideNotificationContainers();

        monthlyNotificationsPage.setVisible(true);
        monthlyNotificationsPage.setManaged(true);

        monthlyNotificationsContainer.getChildren().clear();

        updateNotificationsContainer();
    }

    @FXML
    protected void onViewAllNotifications() {
        hideNotificationContainers();

        allNotificationsPage.setVisible(true);
        allNotificationsPage.setManaged(true);

        allNotificationsContainer.getChildren().clear();

        updateNotificationsContainer();
    }

    @FXML
    protected void onViewIgnoredNotifications() {
        hideNotificationContainers();

        ignoredNotificationsPage.setVisible(true);
        ignoredNotificationsPage.setManaged(true);

        ignoredNotificationsContainer.getChildren().clear();

        updateNotificationsContainer();
    }
}
