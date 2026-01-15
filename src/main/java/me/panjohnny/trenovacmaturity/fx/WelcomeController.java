package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import me.panjohnny.trenovacmaturity.MaturitaApplication;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WelcomeController implements BaseController {
    @FXML
    public HBox lastOpened;

    @Override
    public void loadAppData() {
        String opened = application.getRetentionHelper().get("lastOpened");

        if (opened != null) {
            Button temp = (Button) lastOpened.getChildren().getFirst();
            String meta = application.getZIPMeta(new File(opened));

            if (meta != null) {
                temp.setText(meta);

                temp.setOnAction(event -> {
                    System.out.println("Opening " + opened);
                });
            }
        }
    }
    @FXML
    protected void openPDF() {
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

    @FXML
    protected void openZIP() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open exam ZIP");
        fileChooser.setInitialDirectory(new File(""));
        fileChooser.setSelectedExtensionFilter(new FileChooser.ExtensionFilter("Exam ZIP Files", "*.zip"));
        File file = fileChooser.showOpenDialog(null);

        if (file != null) {
            application.getRetentionHelper().put("lastOpened", file.getAbsolutePath());
            application.openExamZIP(file);
        }
    }

    @FXML
    protected void openCERMAT() {
        String url = "https://maturita.cermat.cz/menu/testy-a-zadani-z-predchozich-obdobi";
        if(Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)){
            Desktop desktop = Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {

            }
        }else{
            Runtime runtime = Runtime.getRuntime();
            try {
                if (System.getProperty("os.name").toLowerCase().contains("mac")) {
                    runtime.exec("open " + url);
                } else {
                    runtime.exec("xdg-open " + url);
                }
            } catch (IOException e) {
            }
        }
    }

    private MaturitaApplication application;
    @Override
    public void setApplication(MaturitaApplication application) {
        this.application = application;
    }
}
