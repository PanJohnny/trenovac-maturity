package me.panjohnny.trenovacmaturity;

import javafx.application.Platform;
import javafx.scene.control.Alert;

import static me.panjohnny.trenovacmaturity.MaturitaApplication.LOGGER;

import java.lang.System.Logger.Level;

public class ExceptionHandler {

    public static void init() {
        // Set up global exception handler
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            LOGGER.log(Level.ERROR, "Uncaught exception in thread " + thread.getName(), throwable);

        });
    }

    public static void handleSevere(Throwable t, String msg) {
        LOGGER.log(Level.ERROR, msg, t);
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Unexpected Error");
            String content = t.getMessage() != null ? t.getMessage() : "No additional information available.";
            alert.setHeaderText(msg);
            alert.setContentText(content);
            alert.showAndWait();
            Platform.exit();
            System.exit(1);
        });
    }

    public static void handleError(Throwable t, String msg) {
        LOGGER.log(Level.ERROR, msg, t);
        showErrorDialog("Error", msg, t.getMessage());
    }

    public static void handleWarning(Throwable t, String msg) {
        LOGGER.log(Level.WARNING, msg, t);
            showErrorDialog("Warning", msg, t.getMessage());
    }

    private static void showErrorDialog(String title, String header, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText(content);
            alert.showAndWait();
        });
    }
}
