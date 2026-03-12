package me.panjohnny.trenovacmaturity.fx;

import javafx.application.Platform;
import javafx.scene.control.Dialog;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import me.panjohnny.trenovacmaturity.ExceptionHandler;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.View;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class Actions {
    public static void openPDF(MaturitaApplication application) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open PDF");
        fileChooser.setInitialDirectory(new File(""));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            application.exam().loadPDF(file);
        }
    }

    public static void openZIP(MaturitaApplication application) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open .maturita file");
        fileChooser.setInitialDirectory(new File(""));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("MATURITA Files", "*.maturita"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            application.getRetentionHelper().put("lastOpened", file.getAbsolutePath());
            application.exam().openExamZIP(file);
        }
    }

    public static void openCERMAT() {
        String url = "https://maturita.cermat.cz/menu/testy-a-zadani-z-predchozich-obdobi";
        openFileOrUrl(null, url);
    }

    private static void openFileOrUrl(File file, String url) {
        if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
            Desktop desktop = Desktop.getDesktop();
            try {
                if (file == null) {
                    desktop.browse(new URI(url));
                } else {
                    desktop.open(file);
                }
            } catch (IOException | URISyntaxException e) {
                ExceptionHandler.handleWarning(e, "Failed to open file or URL");
            }
        } else{
            ProcessBuilder processBuilder = new ProcessBuilder();
            try {
                if (file != null) {
                    url = file.getAbsolutePath();
                }

                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    processBuilder.command("open", url);
                } else {
                    processBuilder.command("xdg-open", url);
                }
                processBuilder.start();
            } catch (IOException e) {
                ExceptionHandler.handleWarning(e, "Failed to open file or URL");
            }
        }
    }

    public static void openAnswerSetPDF(MaturitaApplication application) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Answer Set PDF");
        fileChooser.setInitialDirectory(new File(""));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("PDF Files", "*.pdf"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            application.exam().openAnswerSetPDF(file);
        }
    }

    public static void closeApplication() {
        Platform.exit();
    }

    public static void closeExam(MaturitaApplication application) {
        application.saveOpened();
        application.changeView(View.WELCOME);
    }

    public static void openArchive(MaturitaApplication application) { // TODO: make this work for training
        openFileOrUrl(application.exam().getArchivePath(), null);
    }

    public static void openMeta(MaturitaApplication application) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Informace o maturitě");
        String data = "Název testu: %s%nSoubor:%s".formatted(application.getExam().getMeta(), application.exam().getArchivePath());
        dialog.setContentText(data);
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK);
        dialog.initOwner(application.getPrimaryStage().getOwner());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.showAndWait();
    }

    public static void openInfo(MaturitaApplication application) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Informace o projektu");
        dialog.setHeaderText("Trénovač na Maturitu");
        dialog.setContentText("""
                Verze: %s
                Licence: MIT
                
                Tato aplikace má sloužit k procvičování maturitních otázek v maturitních testech. Pomocí štítků k nim můžete přikládat různé kategorie (např. kategorie příkladů v matematice) a díky tomu se fokusovat na to, co nám nejde.
                
                Upozornění: Aplikace ani její tvůrci nejsou nijak spojení s CERMAT. Uživatel testy, jenž jsou předmětem autorského práva, dodává sám.
                """.formatted(MaturitaApplication.VERSION));
        dialog.getDialogPane().getButtonTypes().addAll(javafx.scene.control.ButtonType.OK);
        dialog.initOwner(application.getPrimaryStage().getOwner());
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.showAndWait();
    }

    public static void openTraining(MaturitaApplication application) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open TRAINING");
        fileChooser.setInitialDirectory(new File(""));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("MATURITA Files", "*.maturita"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            application.getRetentionHelper().put("lastOpened", file.getAbsolutePath());
            application.training().loadTraining(file);
        }
    }

    public static void createTraining(MaturitaApplication application) {
        application.trainingOpenExamSelector();
    }

    public static void startPanicMode(MaturitaApplication application) {
        application.changeScene("training-panic-start-view.fxml");
    }
}
