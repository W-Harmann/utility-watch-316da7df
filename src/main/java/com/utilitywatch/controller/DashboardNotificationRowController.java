package com.utilitywatch.controller;

import com.utilitywatch.notifications.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.format.DateTimeFormatter;

/** fills in one notification row on the home dashboard using a shortened format */
public class DashboardNotificationRowController {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("MMM d h:mma");

    @FXML
    private Label notificationTitleLabel;
    @FXML
    private Label notificationMessageLabel;

    /** @param notification the goal notification to show */
    public void setData(Notification notification) {
        // for example: 'Goal Completed: Reduced Water Usage' becomes 'Water goal completed'
        notificationTitleLabel.setText(shortTitle(notification.getTitle()));
        notificationMessageLabel.setText(notification.getDateTime().format(TIME_FORMAT) + " - " + notification.getMessage());
    }

    // picks the resource out of the stored title and builds a short title
    private String shortTitle(String fullTitle) {

        if (fullTitle.contains("Energy")) {
            return "Energy " + fullTitle.split(":")[0].toLowerCase();
        }
        else if (fullTitle.contains("Water")) {
            return "Water " + fullTitle.split(":")[0].toLowerCase();
        }

        return fullTitle;
    }
}