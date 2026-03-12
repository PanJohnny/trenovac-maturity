package me.panjohnny.trenovacmaturity;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import me.panjohnny.trenovacmaturity.fs.Archiver;
import me.panjohnny.trenovacmaturity.fs.MaturitaFile;
import me.panjohnny.trenovacmaturity.fs.TemporaryFileSystemManager;
import me.panjohnny.trenovacmaturity.fx.BaseController;

import me.panjohnny.trenovacmaturity.image.ImageCache;
import me.panjohnny.trenovacmaturity.model.AnswerSet;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;
import me.panjohnny.trenovacmaturity.model.Training;
import me.panjohnny.trenovacmaturity.pdf.AnswerSetParser;
import me.panjohnny.trenovacmaturity.pdf.ExamPDFParser;
import me.panjohnny.trenovacmaturity.model.TrainingBuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MaturitaApplication extends Application {
    private final double width;
    private final double height;

    private Stage primaryStage;
    private final RetentionHelper helper;
    private final ExamPDFParser examPDFParser;
    private final AnswerSetParser answerSetParser;

    public static final String VERSION = "alpha0.1";

    private boolean assigningInProgress = false;

    private Exam exam;
    private QuestionAnswerMap questionAnswerMap;
    private AnswerSet answers;

    private TrainingBuilder trainingBuilder;
    private Training training;

    public static final System.Logger LOGGER = System.getLogger(MaturitaApplication.class.getName());

    private File archivePath;

    public MaturitaApplication() {
        this.helper = new RetentionHelper(new File("maturita-helper.properties"));
        this.examPDFParser = new ExamPDFParser();
        this.answerSetParser = new AnswerSetParser();

        width = 980;
        height = 720;
    }

    @Override
    public void start(Stage stage) {
        ExceptionHandler.init();

        primaryStage = stage;

        stage.setOnShown(_ -> stage.centerOnScreen());

        stage.setWidth(width);
        stage.setHeight(height);

        changeScene("welcome-view.fxml");
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

    public void loadPDF(File file) {
        loadingScreen();
        examPDFParser.parseAsync(file).handleAsync((exam, t) -> {
            if (t != null) {
                ExceptionHandler.handleWarning(t, "Failed to parse an exam PDF");
            }

            this.exam = exam;
            LOGGER.log(System.Logger.Level.INFO, "Imported exam from PDF");

            homeScreen();
            return null;
        });
    }

    public void saveCurrentOpenedExam() {
        try {
            if (exam == null) {
                return;
            }

            if (training != null) {
                Archiver.createTrainingArchive(training);
            } else {
                Archiver.createArchive(exam.getMeta(), exam, answers, questionAnswerMap, archivePath.toPath());
            }
        } catch (IOException e) {
            ExceptionHandler.handleError(e, "Failed to save current opened exam");
        }
    }

    public void loadQuestionAnswerMap() {
        this.questionAnswerMap = answers.autoAssign(exam);
    }

    public QuestionAnswerMap getQuestionAnswerMap() {
        return questionAnswerMap;
    }

    public void homeScreen() {
        changeScene("home-view.fxml");
    }

    public void trainingOpenExamSelector() {
        changeScene("training-select-exam-view.fxml");
    }

    private void loadingScreen() {
        changeScene("loading.fxml");
    }

    public String getZIPMeta(File file) {
        try {
            if (!Files.exists(file.toPath())) {
                return null;
            }
            ZipInputStream input = new ZipInputStream(new FileInputStream(file));
            ZipEntry entry;

            while ((entry = input.getNextEntry()) != null) {
                if (entry.getName().equals("meta.txt")) {
                    // read meta.txt
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = input.read(buf)) != -1) {
                        baos.write(buf, 0, len);
                    }
                    return baos.toString(java.nio.charset.StandardCharsets.UTF_8);
                }
                input.closeEntry();
            }
        } catch (IOException e) {
            ExceptionHandler.handleWarning(e, "Failed to read .MATURITA metadata");
        }
        return null;
    }

    public void openExamZIP(File file) {
        loadingScreen();
        try {
            if (!assigningInProgress) {
                TemporaryFileSystemManager.cleanup();
            }
        } catch (IOException e) {
            ExceptionHandler.handleWarning(e, "Failed to clean up temporary file system");
        }
        Archiver.loadArchiveAsync(file.toPath()).handleAsync((maturitaFile, t) -> {
            if (t != null) {
                ExceptionHandler.handleSevere(t, "Failed to load exam from .MATURITA file");
            }
            this.exam = maturitaFile.exam();

            if (!assigningInProgress) {
                this.answers = maturitaFile.answerSet();
                this.questionAnswerMap = maturitaFile.qaMap();
            }

            LOGGER.log(System.Logger.Level.INFO, "Loaded exam from .MATURITA file");
            this.archivePath = file;
            if (assigningInProgress) {
                changeScene("import-assign.fxml");
            } else {
                homeScreen();
            }
            return null;
        });
    }

    public File getArchivePath() {
        return archivePath;
    }

    public void openAnswerSetPDF(File file) {
        try {
            loadingScreen();
            answerSetParser.parseAsync(file).handleAsync((an, t) -> {
                if (t != null) {
                    ExceptionHandler.handleSevere(t, "Failed to parse an answer set PDF");
                }
                this.answers = an;
                assigningInProgress = true;

                LOGGER.log(System.Logger.Level.INFO, "Answers parsed from PDF");
                changeScene("import-view.fxml");
                return null;
            });
        } catch (Exception e) {
            ExceptionHandler.handleSevere(e, "Failed to load answer set PDF");
        }
    }

    public RetentionHelper getRetentionHelper() {
        return helper;
    }

    public Exam getExam() {
        return exam;
    }

    public AnswerSet getAnswers() {
        return answers;
    }

    public void closeExam() {
        saveCurrentOpenedExam();
        changeScene("welcome-view.fxml");
        this.exam = null;
        this.answers = null;
        this.questionAnswerMap = null;
        this.assigningInProgress = false;
        this.training = null;
        ImageCache.getInstance().clear();
    }

    @Override
    public void stop() throws Exception {
        closeExam();
        TemporaryFileSystemManager.cleanup();
    }

    public void startTrainingCreation(List<File> files) throws IOException {
        List<MaturitaFile> maturitaFiles = new ArrayList<>();
        for (File file : files) {
            maturitaFiles.add(Archiver.loadArchive(file.toPath()));
        }

        trainingBuilder = new TrainingBuilder(maturitaFiles);
        trainingBuilder.createTagCache();
        changeScene("training-create-view.fxml");
    }

    public TrainingBuilder getTrainingBuilder() {
        return trainingBuilder;
    }

    public void openTraining(Training training) {
        this.training = training;
        this.trainingBuilder = null;
        this.exam = training;
        this.answers = training.getAnswers();
        this.questionAnswerMap = training.getQaMap();

        homeScreen();
    }

    public void loadTraining(File file) {
        loadingScreen();
        try {
            if (!assigningInProgress) {
                TemporaryFileSystemManager.cleanup();
            }
        } catch (IOException e) {
            ExceptionHandler.handleWarning(e, "Failed to clean up temporary file system");
        }
        Archiver.loadTrainingAsync(file.toPath()).handleAsync((training, t) -> {
            if (t != null) {
                ExceptionHandler.handleSevere(t, "Failed to load exam from .MATURITA file");
            }

            LOGGER.log(System.Logger.Level.INFO, "Loaded training from .MATURITA file");

            this.openTraining(training);

            return null;
        });
    }

    public void startPanic(List<File> files) throws IOException {
        List<MaturitaFile> maturitaFiles = new ArrayList<>();
        for (File file : files) {
            maturitaFiles.add(Archiver.loadArchive(file.toPath()));
        }

        trainingBuilder = new TrainingBuilder(maturitaFiles);
        this.training = trainingBuilder.create("Panika!");
        this.exam = training;
        this.answers = training.getAnswers();
        this.questionAnswerMap = training.getQaMap();
        changeScene("training-panic-view.fxml");
    }

    public Training getTraining() {
        return training;
    }
}
