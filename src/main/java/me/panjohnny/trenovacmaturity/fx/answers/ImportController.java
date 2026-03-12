package me.panjohnny.trenovacmaturity.fx.answers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import me.panjohnny.trenovacmaturity.fx.BaseController;
import me.panjohnny.trenovacmaturity.model.answer.AnswerSet;

import java.io.File;

public class ImportController extends BaseController {
    @FXML
    public HBox lastOpened;

    @FXML
    public TextArea metaArea;

    @Override
    public void loadAppData() {
        String opened = application.getRetentionHelper().get("lastOpened");
        AnswerSet answers = application.getAnswers();

        metaArea.setText(answers.getMeta());

        if (opened != null) {
            Button temp = (Button) lastOpened.getChildren().getFirst();
            String meta = application.getZIPMeta(new File(opened));

            if (meta != null) {
                temp.setText(meta);

                temp.setOnAction(_ -> {
                    application.exam().openExamZIP(new File(opened));
                });
            }
        }
    }
}
