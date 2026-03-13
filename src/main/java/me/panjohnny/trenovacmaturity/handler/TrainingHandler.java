package me.panjohnny.trenovacmaturity.handler;

import me.panjohnny.trenovacmaturity.ExceptionHandler;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.View;
import me.panjohnny.trenovacmaturity.fs.Archiver;
import me.panjohnny.trenovacmaturity.fs.MaturitaFile;
import me.panjohnny.trenovacmaturity.fs.TemporaryFileSystemManager;
import me.panjohnny.trenovacmaturity.model.training.Training;
import me.panjohnny.trenovacmaturity.model.training.TrainingBuilder;
import me.panjohnny.trenovacmaturity.pdf.TrainingExporter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static me.panjohnny.trenovacmaturity.MaturitaApplication.LOGGER;

/**
 * Handles everything regarding trainings.
 */
@ForViews({View.TRAINING_PANIC, View.TRAINING_SELECT_EXAM, View.TRAINING_PANIC_START, View.TRAINING_PANIC, View.IN_EXAM, View.TRAINING_CREATE})
public class TrainingHandler extends Handler {
    private TrainingBuilder trainingBuilder;

    public TrainingHandler(MaturitaApplication application) {
        super(application);
    }

    @Override
    protected void freeNonDefaults() {
        trainingBuilder = null;
    }

    @Override
    public void save() throws IOException {
        Archiver.createTrainingArchive(training);
    }

    public void startTrainingCreation(List<File> files) throws IOException {
        List<MaturitaFile> maturitaFiles = new ArrayList<>();
        for (File file : files) {
            maturitaFiles.add(Archiver.loadArchive(file.toPath()));
        }

        this.trainingBuilder = new TrainingBuilder(maturitaFiles);
        trainingBuilder.createTagCache();
        application.changeView(View.TRAINING_CREATE);
    }

    public TrainingBuilder getTrainingBuilder() {
        return trainingBuilder;
    }

    public void loadTraining(File file) {
        application.changeView(View.LOADING);
        archivePath = file;
        try {
            TemporaryFileSystemManager.cleanup();
        } catch (IOException e) {
            ExceptionHandler.handleWarning(e, "Failed to clean up temporary file system");
        }

        Archiver.loadTrainingAsync(file.toPath()).handleAsync((training, t) -> {
            if (t != null) {
                ExceptionHandler.handleSevere(t, "Failed to load training from .MATURITA file");
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
        training.markTemporary();
        this.exam = training;
        this.answers = training.getAnswers();
        this.questionAnswerMap = training.getQaMap();

        application.changeView(View.TRAINING_PANIC);
    }

    public void openTraining(Training training) {
        this.training = training;
        this.trainingBuilder = null;
        this.exam = training;
        this.answers = training.getAnswers();
        this.questionAnswerMap = training.getQaMap();

        application.changeView(View.IN_EXAM);
    }

    public void export() {
        TrainingExporter.createPDF(training);
    }
}
