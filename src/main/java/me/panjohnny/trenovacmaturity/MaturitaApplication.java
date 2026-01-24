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

import me.panjohnny.trenovacmaturity.model.AnswerSet;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;
import me.panjohnny.trenovacmaturity.pdf.AnswerSetParser;
import me.panjohnny.trenovacmaturity.pdf.ExamPDFParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MaturitaApplication extends Application {
    private Stage primaryStage;
    private final RetentionHelper helper;
    private final ExamPDFParser examPDFParser;
    private final AnswerSetParser answerSetParser = new AnswerSetParser();

    private boolean assigningInProgress = false;

    private Exam exam;
    private QuestionAnswerMap questionAnswerMap;
    private AnswerSet answers;

    public MaturitaApplication() {
        this.helper = new RetentionHelper(new java.io.File("maturita-helper.properties"));
        this.examPDFParser = new ExamPDFParser();
    }

    @Override
    public void start(Stage stage) throws IOException {
        primaryStage = stage;
        changeScene("welcome-view.fxml");
        stage.setTitle("Trénovač maturity");
        Application.setUserAgentStylesheet(new NordDark().getUserAgentStylesheet());
        stage.show();
    }

    public void changeScene(String resource) {
        Platform.runLater(() -> {
            FXMLLoader fxmlLoader = new FXMLLoader(MaturitaApplication.class.getResource(resource));
            Scene scene = null;
            try {
                scene = new Scene(fxmlLoader.load(), 720, 580);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            scene.getStylesheets().add(Launcher.class.getResource("styles.css").toExternalForm());

            BaseController controller = fxmlLoader.getController();
            if (controller != null) {
                controller.setApplication(this);
                controller.loadAppData();
            }


            primaryStage.setScene(scene);
        });
    }

    private MaturitaFile loadedFile;

    public void loadPDF(File file) {
        try {
            loadingScreen();
            examPDFParser.parseAsync(file).handleAsync((exam, t) -> {
                if (t != null) {
                    throw new RuntimeException(t);
                }

                this.exam = exam;
                return null;
            }).thenRun(this::homeScreen);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void saveQuestionAnswerMap() {
        try {
            Archiver.createArchive(exam.getMeta(), exam, answers, questionAnswerMap);
        } catch (Exception e) {
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
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
            throw new RuntimeException(e);
        }
        Archiver.loadArchiveAsync(file.toPath()).handleAsync((maturitaFile, t) -> {
            if (t != null) {
                throw new RuntimeException(t);
            }
            this.exam = maturitaFile.exam();
            if (!assigningInProgress) {
                this.answers = maturitaFile.answerSet();
                this.questionAnswerMap = maturitaFile.qaMap();
            }
            System.out.println("exam loaded from zip");
            return null;
        }).thenRun(() -> {
            if (assigningInProgress) {
                changeScene("import-assign.fxml");
            } else {
                homeScreen();
            }
        });
    }

    public void openAnswerSetPDF(File file) {
        try {
            loadingScreen();
            answerSetParser.parseAsync(file).handleAsync((an, t) -> {
                if (t != null) {
                    throw new RuntimeException(t);
                }
                this.answers = an;
                System.out.println("answers loaded from PDF");
                assigningInProgress = true;
                return null;
            }).thenRun(() -> {
                changeScene("import-view.fxml");
            });
        } catch (Exception e) {
            throw new RuntimeException(e);
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
}
