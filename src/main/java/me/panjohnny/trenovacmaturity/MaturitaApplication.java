package me.panjohnny.trenovacmaturity;

import atlantafx.base.theme.NordDark;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.panjohnny.trenovacmaturity.archive.ArchiveLoader;
import me.panjohnny.trenovacmaturity.fx.BaseController;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.pdf.PDFExtractor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class MaturitaApplication extends Application {
    private Stage primaryStage;
    private final RetentionHelper helper;
    private final PDFExtractor extractor;
    public static final Path relativeLocation = Path.of("generated/temp/");

    public MaturitaApplication() {
        this.helper = new RetentionHelper(new java.io.File("maturita-helper.properties"));
        this.extractor = new PDFExtractor();
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

    private Exam exam;

    public void loadPDF(File file) {
        try {
            loadingScreen();
            extractor.parseAsync(file).handleAsync((exam, t) -> {
                if (t != null) {
                    throw new RuntimeException(t);
                }
                this.exam = exam;
                System.out.println("exam loaded from PDF");
                return null;
            }).thenRun(this::homeScreen);

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void homeScreen() {
        changeScene("home-view.fxml");
    }

    private void loadingScreen() {
        changeScene("loading.fxml");
    }

    public String getZIPMeta(File file) {
        try {
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
        ArchiveLoader.loadExamFromArchiveAsync(file).handleAsync((exam, t) -> {
            if (t != null) {
                throw new RuntimeException(t);
            }
            this.exam = exam;
            System.out.println("exam loaded from zip");
            return null;
        }).thenRun(this::homeScreen);
    }

    public RetentionHelper getRetentionHelper() {
        return helper;
    }

    public Exam getExam() {
        return exam;
    }
}
