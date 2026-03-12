package me.panjohnny.trenovacmaturity.fx.training;

import javafx.fxml.FXML;
import me.panjohnny.trenovacmaturity.ExceptionHandler;

import java.io.IOException;

public class TrainingPanicStartController extends TrainingSelectExamController {

    @FXML
    @Override
    public void nextStep() {
        try {
            application.training().startPanic(super.selectedExamFiles);
        } catch (IOException e) {
            ExceptionHandler.handleError(e, "Training creation failed");
        }
    }


}
