package com.utilitywatch;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * The JavaFX entry point. It opens the login screen ({@code login-view.fxml}) in a window with the
 * app's stylesheet applied.
 */
public class Main extends Application {
    /** The window title. */
    public static final String TITLE = "Utility Watch";
    /** The width of the first window, in pixels. */
    public static final int WIDTH = 900;
    /** The height of the first window, in pixels. */
    public static final int HEIGHT = 600;

    /**
     * Opens the login screen in the given window. Called by JavaFX once the toolkit has started.
     *
     * @param stage the main window supplied by JavaFX
     * @throws IOException if {@code login-view.fxml} cannot be loaded
     */
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), WIDTH, HEIGHT);

        String stylesheet = Main.class.getResource("stylesheet.css").toExternalForm();
        scene.getStylesheets().add(stylesheet);

        stage.setTitle(TITLE);
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Starts the JavaFX application.
     *
     * @param args the command-line arguments, passed on to JavaFX
     */
    public static void main(String[] args) {
        launch(args);
    }
}