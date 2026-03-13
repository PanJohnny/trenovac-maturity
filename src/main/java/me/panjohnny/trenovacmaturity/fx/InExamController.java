package me.panjohnny.trenovacmaturity.fx;

import atlantafx.base.controls.Popover;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.stage.*;
import me.panjohnny.trenovacmaturity.TagHelper;
import me.panjohnny.trenovacmaturity.View;
import me.panjohnny.trenovacmaturity.fx.node.TagGridCell;
import me.panjohnny.trenovacmaturity.model.answer.Answer;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;
import me.panjohnny.trenovacmaturity.model.training.Training;
import org.controlsfx.control.GridView;

import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class InExamController extends BaseController {
    @FXML
    private MenuBar menuBar;

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
    private ProgressBar timeBar;

    @FXML
    private Label timeLabel;

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

    protected void redraw() {
        if (exam == null || exam.getCurrentQuestion() == null) {
            application.changeView(View.WELCOME);
            return;
        }

        double width = exam.getCurrentQuestion().image().getWidth();
        double height = exam.getCurrentQuestion().image().getHeight();

        tagInput.setText(exam.getCurrentQuestion().getTagString());

        int CANVAS_MAX_WIDTH = 1000;
        if (width > CANVAS_MAX_WIDTH) {
            double scale = (double) CANVAS_MAX_WIDTH / width;
            width = CANVAS_MAX_WIDTH;
            height = (int) (height * scale);
        }

        int CANVAS_MAX_HEIGHT = 700;
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

    protected Exam exam;
    private Timer timer;
    protected Training training;

    @Override
    public void loadAppData() {
        stopTimer();

        exam = application.getExam();
        training = application.getTraining();

        if (training != null) {
            welcomeText.setText("Trénink: " + training.getMeta());

            Menu trainingMenu = new Menu("Trénink");
            MenuItem exportItem = new MenuItem("Exportovat jako PDF");
            exportItem.setOnAction(_ -> {
                application.training().export();
            });
            trainingMenu.getItems().add(exportItem);

            menuBar.getMenus().add(1, trainingMenu);
        }

        redraw();
    }

    @FXML
    private MenuItem saveMenuItem;

    public void initialize() {
        KeyCombination save = new KeyCodeCombination(javafx.scene.input.KeyCode.S, KeyCombination.CONTROL_DOWN);
        saveMenuItem.setAccelerator(save);
    }

    @FXML
    public void save() {
        application.saveOpened();
    }

    protected int timeSeconds = 0;
    protected int timeLimitSeconds = 60 * 135; // 135 mimut

    @FXML
    private void startTimer() {
        // ask the user how many minutes they want to set for the timer
        String defaultTime = "135";
        if (training != null) {
            defaultTime = "15";
            timeLimitSeconds = 15 * 60;
        }
        TextInputDialog dialog = new TextInputDialog(defaultTime);
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

        startTimerCount();
    }

    protected void startTimerCount() {
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
    public void openArchive() {
        Actions.openArchive(application);
    }

    @FXML
    public void openMeta() {
        Actions.openMeta(application);
    }

    private Popover tagPopover;
    private GridView<String> tagGrid;

    public void tagInputChanged() {
        String value = tagInput.getText();
        exam.getCurrentQuestion().setTags(value);

        if (tagPopover == null) {
            tagGrid = new GridView<>();
            tagGrid.setMinSize(300, 100);
            tagPopover = new Popover(tagGrid);
            tagPopover.setTitle("Nabídka štítků");
            tagPopover.setAutoHide(true);

            tagGrid.setCellFactory(_ -> new TagGridCell(this));
        }

        tagGrid.getItems().clear();
        for (String tag : TagHelper.suggest(value)) {
            tagGrid.getItems().add(tag);
        }

        if (!tagPopover.isShowing()) {
            tagPopover.show(tagInput);
        }
    }

    public void addTag(String tag) {
        String inp = tagInput.getText();
        int lastIndexOfComma = inp.lastIndexOf(',');

        if (lastIndexOfComma > 0) {
            inp = inp.substring(0, lastIndexOfComma);
            inp += "," + tag;
            tagInput.setText(inp + ",");
        } else {
            tagInput.setText(tag + ",");
        }

        tagInput.positionCaret(tagInput.getText().length());
        tagInputChanged();
    }

    @FXML
    public void info() {
        Actions.openInfo(application);
    }

    @FXML
    protected void changeName() {
        exam.setMeta(examText.getText());
    }
}
