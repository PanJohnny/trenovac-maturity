package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import java.awt.*;
import java.io.File;

public class WelcomeController extends BaseController {
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
                    application.exam().openExamZIP(new File(opened));
                });
            }
        }
    }
}
