package me.panjohnny.trenovacmaturity;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import me.panjohnny.trenovacmaturity.fs.TemporaryFileSystemManager;
import me.panjohnny.trenovacmaturity.fs.ZIPInfo;
import me.panjohnny.trenovacmaturity.fx.BaseController;

import me.panjohnny.trenovacmaturity.handler.ExamHandler;
import me.panjohnny.trenovacmaturity.handler.ForViews;
import me.panjohnny.trenovacmaturity.handler.Handler;
import me.panjohnny.trenovacmaturity.handler.TrainingHandler;
import me.panjohnny.trenovacmaturity.image.ImageCache;
import me.panjohnny.trenovacmaturity.model.answer.AnswerSet;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;
import me.panjohnny.trenovacmaturity.model.training.Training;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MaturitaApplication extends Application {
    private final double width;
    private final double height;

    private Stage primaryStage;
    private final RetentionHelper helper;

    public static final String VERSION = "alpha0.1";

    public static final System.Logger LOGGER = System.getLogger(MaturitaApplication.class.getName());


    private TrainingHandler trainingHandler;
    private ExamHandler examHandler;
    private Handler currentHandler;

    public MaturitaApplication() {
        this.helper = new RetentionHelper(new File("maturita-helper.properties"));

        this.trainingHandler = new TrainingHandler(this);
        this.examHandler = new ExamHandler(this);

        width = 980;
        height = 720;
    }

    public ExamHandler exam() {
        if (this.currentHandler == trainingHandler) {
            trainingHandler.free();
        }
        this.currentHandler = examHandler;
        return examHandler;
    }

    public TrainingHandler training() {
        if (this.currentHandler == examHandler) {
            trainingHandler.free();
        }
        this.currentHandler = trainingHandler;
        return trainingHandler;
    }

    public void changeView(View view) {
        changeScene(view.toString());

        if (this.currentHandler == null || view == View.LOADING)
            return;

        var annotation = currentHandler.getClass().getAnnotation(ForViews.class);
        if (annotation != null) {
            boolean isPresent = false;
            for (View view1 : annotation.value()) {
                if (view1 == view) {
                    isPresent = true;
                    break;
                }
            }

            if (!isPresent) {
                currentHandler.free();
                ImageCache.getInstance().clear();
                currentHandler = null;
            }
        }
    }

    @Override
    public void start(Stage stage) {
        ExceptionHandler.init();

        primaryStage = stage;

        stage.setOnShown(_ -> stage.centerOnScreen());

        stage.setWidth(width);
        stage.setHeight(height);

        changeView(View.WELCOME);
        stage.setTitle("Trénovač maturity");
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        stage.show();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void changeScene(String resource) {
        Platform.runLater(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(MaturitaApplication.class.getResource(resource));
            Scene scene = null;
            try {
                scene = new Scene(fxmlLoader.load());
            } catch (IOException e) {
                ExceptionHandler.handleSevere(e, "Failed to load scene file");
            }

            assert scene != null;
            scene.getStylesheets().add(Objects.requireNonNull(Launcher.class.getResource("styles.css")).toExternalForm());

            BaseController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setApplication(this);
                controller.loadAppData();
            }

            primaryStage.setScene(scene);
        });
    }

    public void saveOpened() {
        try {
            assert currentHandler != null;
            currentHandler.save();
        } catch (IOException e) {
            ExceptionHandler.handleError(e, "Failed to save current opened exam");
        }
    }

    public void trainingOpenExamSelector() {
        changeScene("training-select-exam-view.fxml");
    }

    public ZIPInfo getZIPInfo(File file) {
        try {
            if (!Files.exists(file.toPath())) {
                return null;
            }
            ZipInputStream input = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;

            String meta = null;
            boolean isTraining = false;

            while ((entry = input.getNextEntry()) != null) {
                if (entry.getName().equals("meta.txt")) {
                    // read meta.txt
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = input.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                    meta = baos.toString(java.nio.charset.StandardCharsets.UTF_8);
                } else if (entry.getName().equals("training.json")) {
                    isTraining = true;
                }
                input.closeEntry();
            }

            return new ZIPInfo(meta, isTraining);
        } catch (IOException e) {
            ExceptionHandler.handleWarning(e, "Failed to read .MATURITA metadata");
        }
        return null;
    }

    public RetentionHelper getRetentionHelper() {
        return helper;
    }

    public Exam getExam() {
        if (currentHandler == null) {
            throw new IllegalStateException("Illegal state reached: handler not present");
        }

        return currentHandler.getExam();
    }

    public QuestionAnswerMap getQuestionAnswerMap() {
        if (currentHandler == null) {
            throw new IllegalStateException("Illegal state reached: handler not present");
        }

        return currentHandler.getQuestionAnswerMap();
    }

    public AnswerSet getAnswers() {
        if (currentHandler == null) {
            throw new IllegalStateException("Illegal state reached: handler not present");
        }

        return currentHandler.getAnswers();
    }

    public @Nullable Training getTraining() {
        if (currentHandler == null) {
            throw new IllegalStateException("Illegal state reached: handler not present");
        }

        return currentHandler.getTraining();
    }

    public File getArchivePath() {
        if (currentHandler == null) {
            throw new IllegalStateException("Illegal state reached: handler not present");
        }

        return currentHandler.getArchivePath();
    }

    @Override
    public void stop() throws Exception {
        if (currentHandler != null) {
            saveOpened();
            currentHandler.free();
            ImageCache.getInstance().clear();
        }
        TemporaryFileSystemManager.cleanup();
    }
}
