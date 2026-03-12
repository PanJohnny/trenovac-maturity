package me.panjohnny.trenovacmaturity.fs;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.fx.LoadingController;
import me.panjohnny.trenovacmaturity.model.*;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Archiver {

    public static void createArchive(String name, Exam exam, @Nullable AnswerSet answersSet, @Nullable QuestionAnswerMap qaMap, @Nullable Path archivePath) throws IOException {
        Path path = archivePath;

        if (path == null) {
            path = Path.of(name + ".maturita");
        }

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            zip.putNextEntry(new ZipEntry("questions.json"));
            zip.write(exam.serialize().toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("meta.txt"));
            zip.write(exam.getMeta().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            for (Question question : exam) {
                zip.putNextEntry(new ZipEntry(question.region_id() + ".png"));
                String url = question.image().getUrl();

                // Opravený způsob načítání ze souboru
                Path imagePath = Path.of(URI.create(url));
                byte[] bytes = Files.readAllBytes(imagePath);
                zip.write(bytes);

                zip.closeEntry();
            }

            if (answersSet != null) {
                zip.putNextEntry(new ZipEntry("answers.json"));
                zip.write(answersSet.serialize().toString().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();

                for (Answer a : answersSet) {
                    zip.putNextEntry(new ZipEntry(a.region_id() + ".png"));
                    String url = a.image().getUrl();

                    Path imagePath = Path.of(URI.create(url));
                    byte[] bytes = Files.readAllBytes(imagePath);
                    zip.write(bytes);

                    zip.closeEntry();
                }

                if (qaMap != null) {
                    zip.putNextEntry(new ZipEntry("qa_map.json"));
                    zip.write(qaMap.serialize().toString().getBytes(StandardCharsets.UTF_8));
                    zip.closeEntry();
                }
            }
        } catch (IOException e) {
            // Vyčistit neúplný soubor
            Files.deleteIfExists(path);
            throw e;
        }

        MaturitaApplication.LOGGER.log(System.Logger.Level.INFO, "Exam written to file {0}", path);
    }

    public static void createTrainingArchive(Training training) throws IOException {
        String name = training.getMeta();
        String fileName = name.trim() + ".training.maturita";

        Path path = Path.of(fileName);

        try (ZipOutputStream zip = new ZipOutputStream(Files.newOutputStream(path))) {
            zip.putNextEntry(new ZipEntry("training.json"));
            zip.write(training.serialize().toString().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            zip.putNextEntry(new ZipEntry("meta.txt"));
            zip.write(training.getMeta().getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();

            for (Question question : training) {
                zip.putNextEntry(new ZipEntry(question.region_id() + ".png"));
                String url = question.image().getUrl();

                // Opravený způsob načítání ze souboru
                Path imagePath = Path.of(URI.create(url));
                byte[] bytes = Files.readAllBytes(imagePath);
                zip.write(bytes);

                zip.closeEntry();
            }

            AnswerSet answersSet = training.getAnswers();
            if (answersSet != null) {
                for (Answer a : answersSet) {
                    zip.putNextEntry(new ZipEntry(a.region_id() + ".png"));
                    String url = a.image().getUrl();

                    Path imagePath = Path.of(URI.create(url));
                    byte[] bytes = Files.readAllBytes(imagePath);
                    zip.write(bytes);

                    zip.closeEntry();
                }
            }
        } catch (IOException e) {
            // Vyčistit neúplný soubor
            Files.deleteIfExists(path);
            throw e;
        }

    }

    public static MaturitaFile loadArchive(Path path) throws IOException {
        ZipInputStream inputStream = new ZipInputStream(Files.newInputStream(path));

        Gson gson = new Gson();

        Exam exam;
        AnswerSet answers = null;
        QuestionAnswerMap qaMap = null;

        JsonElement examEl = null;
        JsonElement answersEl = null;
        JsonElement qaMapEl = null;

        double progress = 0.0d;
        double step = 1.0d / 30d;

        LoadingController.setProgress(progress);

        ZipEntry entry;
        while ((entry = inputStream.getNextEntry()) != null) {
            if (entry.getName().endsWith(".png")) {
                byte[] image = inputStream.readAllBytes();
                String fileName = entry.getName();

                TemporaryFileSystemManager.writeBytes(fileName, image);
            } else if (entry.getName().equals("questions.json")) {
                examEl = gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
            } else if (entry.getName().equals("answers.json")) {
                answersEl = gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
            } else if (entry.getName().equals("qa_map.json")) {
                qaMapEl = gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
            }

            progress += step;
            LoadingController.setProgress(progress);
            inputStream.closeEntry();
        }

        inputStream.close();

        if (examEl != null) {
            exam = Exam.deserialize(examEl);
        } else {
            throw new IOException("Archive is missing questions.json");
        }

        if (answersEl != null) {
            answers = AnswerSet.deserialize(answersEl);

            if (qaMapEl != null) {
                qaMap = QuestionAnswerMap.deserialize(qaMapEl, exam, answers);
            }
        }

        LoadingController.setProgress(1d);

        return new MaturitaFile(path, exam, answers, qaMap);
    }

    public static Training loadTraining(Path path) throws IOException {
        ZipInputStream inputStream = new ZipInputStream(Files.newInputStream(path));

        Gson gson = new Gson();

        Training training;

        JsonElement trainingEl = null;

        double progress = 0.0d;
        double step = 1.0d / 30d;

        LoadingController.setProgress(progress);

        ZipEntry entry;
        while ((entry = inputStream.getNextEntry()) != null) {
            if (entry.getName().endsWith(".png")) {
                byte[] image = inputStream.readAllBytes();
                String fileName = entry.getName();

                TemporaryFileSystemManager.writeBytes(fileName, image);
            } else if (entry.getName().equals("training.json")) {
                trainingEl = gson.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), JsonElement.class);
            }

            progress += step;
            LoadingController.setProgress(progress);
            inputStream.closeEntry();
        }

        inputStream.close();

        if (trainingEl != null) {
            training = Training.deserialize(trainingEl);
        } else {
            throw new IOException("Archive is missing questions.json");
        }

        LoadingController.setProgress(1d);

        return training;
    }

    public static CompletableFuture<MaturitaFile> loadArchiveAsync(Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadArchive(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static CompletableFuture<Training> loadTrainingAsync(Path path) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return loadTraining(path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }
}
