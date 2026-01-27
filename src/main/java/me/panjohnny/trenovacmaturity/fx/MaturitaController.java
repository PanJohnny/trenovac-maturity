package me.panjohnny.trenovacmaturity.fx;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.stage.*;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.model.Answer;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;

import java.util.Arrays;
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

    @FXML
    private TextField tagInput;

    @FXML
    private HBox hboxInfo;

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
                    ImageView iv = new ImageView(answer.image());
                    iv.setPreserveRatio(true);
                    iv.setFitWidth(800);
                    root.getChildren().add(iv);
                    if (text != null && !text.isEmpty()) {
                        Label answerLabel = new Label(text);
                        answerLabel.setWrapText(true);
                        answerLabel.setMaxWidth(800);
                        root.getChildren().add(answerLabel);
                    }
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

        tagInput.setText(exam.getCurrentQuestion().getTagString());

        if (predBox != null) {
            predBox.setVisible(false);
        }

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

    @FXML
    public void closeExam() {
        Actions.closeExam(application);
    }

    @FXML
    public void closeApp() {
        Actions.closeApplication();
    }

    @FXML
    public void openArchive() {
        Actions.openArchive(application);
    }

    @FXML
    public void openMeta() {
        Actions.openMeta(application);
    }

    public void tagInputChanged() {
        String value = tagInput.getText();
        exam.getCurrentQuestion().setQuestions(value);
    }

    @FXML
    public void info() {
        Actions.openInfo(application);
    }

    private VBox predBox;

    @FXML
    public void selectTags() {
        if (predBox != null) {
            predBox.setVisible(true);
            return;
        }
        predBox = new VBox();
        hboxInfo.getChildren().add(predBox);
        predBox.getChildren().addAll(
                new Label("Matematika"),
                addTagButton("Číselné obory"),
                addTagButton("Algebraické výrazy"),
                addTagButton("Rovnice a nerovnice"),
                addTagButton("Funkce"),
                addTagButton("Posloupnosti a finanční matematika"),
                addTagButton("Planimetrie"),
                addTagButton("Stereometrie"),
                addTagButton("Analytická geometrie"),
                addTagButton("Kombinatorika; pravděpodobnost a statistika"),
                new Label("Matematika rozšiřující"),
                addTagButton("Číselné množiny"),
                addTagButton("Analytická geometrie")
        );
    }

    private Button addTagButton(String tag) {
        Button b = new Button(tag);
        b.setOnAction((e) -> {
            tagInput.setText(b.getText() + "," + tagInput.getText());
            tagInputChanged();
        });
        return b;
    }
}
