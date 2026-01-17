package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.pdf.PDFExtractor;

import java.io.File;
import java.io.IOException;

public class MaturitaController implements BaseController{
    @FXML
    private Label welcomeText;

    @FXML
    private Label infoLabel;

    @FXML
    private Canvas canvas;

    private final int CANVAS_MAX_WIDTH = 1000;
    private final int CANVAS_MAX_HEIGHT = 700;

    @FXML
    protected void onPrevClick() {
        if (exam != null) {
            exam.previousQuestion();
            redraw();
        }
    }

    @FXML
    protected void onNextClick() {
        if (exam != null) {
            exam.nextQuestion();
            redraw();
        }
    }

    private void redraw() {
        double width = exam.getCurrentQuestion().image().getWidth();
        double height = exam.getCurrentQuestion().image().getHeight();

        if (width > CANVAS_MAX_WIDTH) {
            double scale = (double) CANVAS_MAX_WIDTH / width;
            width = CANVAS_MAX_WIDTH;
            height = (int) (height * scale);
        }

        if (height > CANVAS_MAX_HEIGHT) {
            double scale = (double) CANVAS_MAX_HEIGHT / height;
            height = CANVAS_MAX_HEIGHT;
            width = (int) (width * scale);
        }

        canvas.setWidth(width);
        canvas.setHeight(height);

        canvas.getGraphicsContext2D().drawImage(exam.getCurrentQuestion().image(), 0, 0, width, height);

        if (exam.getCurrentQuestion().text() != null) {
            welcomeText.setText(exam.getCurrentQuestion().text());
        } else {
            welcomeText.setText("");
        }

        infoLabel.setText(exam.getCurrentQuestion().number() + "/" + exam.length());
    }

    private MaturitaApplication application;
    @Override
    public void setApplication(MaturitaApplication application) {
        this.application = application;
    }

    private Exam exam;

    @Override
    public void loadAppData() {
        exam = application.getExam();
    }
}
