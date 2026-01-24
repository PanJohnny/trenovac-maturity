package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.PopupWindow;
import javafx.stage.Stage;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.model.Answer;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;

import java.util.List;

public class MaturitaController extends BaseController {
    @FXML
    private Label welcomeText;

    @FXML
    private Label infoLabel;

    @FXML
    private Canvas canvas;

    @FXML
    private VBox answerBox;

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

    @FXML
    protected void showAnswer() {
        QuestionAnswerMap map = application.getQuestionAnswerMap();
        if (exam != null && map != null) {
            List<Answer> answers = map.getOrDefault(exam.getCurrentQuestion(), null);

            // create a new window with the answer
            if (answers != null) {
                VBox root = new VBox(10);
                root.setPadding(new Insets(10));
                for (Answer answer : answers) {
                    String text = answer.text();
                    if (text != null && !text.isEmpty()) {
                        Label answerLabel = new Label(text);
                        answerLabel.setWrapText(true);
                        answerLabel.setMaxWidth(800);
                        root.getChildren().add(answerLabel);
                    }
                    ImageView iv = new ImageView(answer.image());
                    iv.setPreserveRatio(true);
                    iv.setFitWidth(800);
                    root.getChildren().add(iv);
                }
                ScrollPane scroll = new ScrollPane(root);
                scroll.setFitToWidth(true);

                answerBox.getChildren().clear();
                answerBox.getChildren().add(scroll);
            }
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
        answerBox.getChildren().clear();
    }

    private Exam exam;

    @Override
    public void loadAppData() {
        exam = application.getExam();
        redraw();
    }
}
