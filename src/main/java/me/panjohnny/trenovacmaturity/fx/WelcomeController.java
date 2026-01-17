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
                    application.openExamZIP(new File(opened));
                });
            }
        }
    }
    @FXML
    protected void openPDF() {
        Actions.openPDF(application);
    }

    @FXML
    protected void openZIP() {
        Actions.openZIP(application);
    }

    @FXML
    protected void openCERMAT() {
        Actions.openCERMAT();
    }

    private MaturitaApplication application;
    @Override
    public void setApplication(MaturitaApplication application) {
        this.application = application;
    }
}
