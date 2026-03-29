package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import me.panjohnny.trenovacmaturity.fs.ZIPInfo;

import java.io.File;

public class WelcomeController extends BaseController {
    @FXML
    public HBox lastOpened;

    @Override
    public void loadAppData() {
        String opened = application.getRetentionHelper().get("lastOpened");

        if (opened != null) {
            Button temp = (Button) lastOpened.getChildren().getFirst();
            ZIPInfo info = application.getZIPInfo(new File(opened));

            if (info == null) {
                return;
            }

            temp.setText(info.meta());

            temp.setOnAction(ignored -> {
                if (info.isTraining()) {
                    application.training().loadTraining(new File(opened));
                } else {
                    application.exam().openExamZIP(new File(opened));
                }
            });
        }
    }
}
