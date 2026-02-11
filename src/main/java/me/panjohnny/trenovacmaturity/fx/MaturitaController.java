package me.panjohnny.trenovacmaturity.fx;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.*;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.model.Answer;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;

import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
    private TextField examText;

    @FXML
    private HBox hboxInfo;

    @FXML
    private ProgressBar timeBar;

    @FXML
    private Label timeLabel;

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
        if (exam == null || exam.getCurrentQuestion() == null) {
            application.homeScreen();
            return;
        }

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
        examText.setText(exam.getMeta());
        answerBox.getChildren().clear();
    }

    private Exam exam;
    private Timer timer;

    @Override
    public void loadAppData() {
        stopTimer();

        exam = application.getExam();
        redraw();
    }

    int timeSeconds = 0;
    int timeLimitSeconds = 60 * 135; // 135 mimut

    @FXML
    private void startTimer() {
        // ask the user how many minutes they want to set for the timer
        TextInputDialog dialog = new TextInputDialog("135");
        dialog.setTitle("Nastavit časovač");
        dialog.setHeaderText("Nastavit časovač");
        dialog.setContentText("Zadejte čas v minutách:");
        dialog.initOwner(application.getPrimaryStage());

        dialog.initModality(Modality.NONE);
        dialog.showAndWait().ifPresent(input -> {
            try {
                int minutes = Integer.parseInt(input);
                timeLimitSeconds = minutes * 60;
            } catch (NumberFormatException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Chyba");
                alert.setHeaderText("Neplatný vstup");
                alert.setContentText("Zadejte platný počet minut.");
                alert.initOwner(application.getPrimaryStage());
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
            }
        });

        if (timer != null) {
            timer.cancel();
        }

        timer = new Timer();

        timeSeconds = 0;
        timeBar.setProgress(0);
        timeBar.getParent().setVisible(true);
        timeBar.getParent().minHeight(10);

        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (application.getExam() == null) {
                    timer.cancel();
                    return;
                }
                timeSeconds++;
                int minutes = timeSeconds / 60;
                int seconds = timeSeconds % 60;
                Platform.runLater(() -> {
                    timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
                    timeBar.setProgress((double) timeSeconds / timeLimitSeconds);
                });

                // check if timer finished
                if (timeSeconds >= timeLimitSeconds) {
                    Platform.runLater(() -> {
                        stopTimer();
                        var g = canvas.getGraphicsContext2D();
                        g.setFont(new Font("system", 50));
                        g.setFill(Paint.valueOf("rgb(255, 0, 0)"));
                        g.fillText("Čas vypršel", 100, 50);
                    });

                    Toolkit.getDefaultToolkit().beep();
                }
            }
        }, 1000, 1000);
    }

    @FXML
    private void stopTimer() {
        if (timer != null) {
            timer.cancel();
        }

        timeBar.getParent().minHeight(0);
        timeBar.getParent().setVisible(false);
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
        b.setOnAction((_unused) -> {
            tagInput.setText(b.getText() + "," + tagInput.getText());
            tagInputChanged();
        });
        return b;
    }

    @FXML
    protected void changeName() {
        exam.setMeta(examText.getText());
    }
}
