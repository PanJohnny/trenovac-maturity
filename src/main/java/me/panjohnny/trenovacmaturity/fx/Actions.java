package me.panjohnny.trenovacmaturity.fx;

import javafx.stage.FileChooser;
import me.panjohnny.trenovacmaturity.MaturitaApplication;

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
            application.getRetentionHelper().put("lastOpenedPDF", file.getAbsolutePath());
            application.loadPDF(file);
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
            application.openExamZIP(file);
        }
    }

    public static void openCERMAT() {
        String url = "https://maturita.cermat.cz/menu/testy-a-zadani-z-predchozich-obdobi";
        if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {

            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            ProcessBuilder processBuilder = new ProcessBuilder();
            try {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    processBuilder.command("open", url);
                } else {
                    processBuilder.command("xdg-open", url);
                }
                processBuilder.start();
            } catch (IOException e) {
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
            application.openAnswerSetPDF(file);
        }
    }
}
