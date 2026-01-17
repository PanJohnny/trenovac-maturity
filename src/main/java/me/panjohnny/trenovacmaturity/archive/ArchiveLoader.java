package me.panjohnny.trenovacmaturity.archive;

import me.panjohnny.trenovacmaturity.fx.LoadingController;
import me.panjohnny.trenovacmaturity.image.ImageCache;
import me.panjohnny.trenovacmaturity.model.Exam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveLoader {
    public static Exam loadExamFromArchive(File file) {
        double progress = 0d;
        double progressStep = 1d / 30d;
        try(ZipInputStream inputStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            String examString = null;
            while ((entry = inputStream.getNextEntry()) != null) {
                progress += progressStep;
                LoadingController.setProgress(progress);
                if (entry.getName().equals("exam.txt")) {
                    examString = new String(inputStream.readAllBytes());
                } else if (entry.getName().endsWith(".png")) {
                    byte[] image = inputStream.readAllBytes();

                    ImageCache.getInstance().putImage(entry.getName().substring(0, entry.getName().length() - 4), image);
                }
            }

            if (examString == null) {
                return null;
            }

            LoadingController.setProgress(1);

            return Exam.deserialize(examString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static CompletableFuture<Exam> loadExamFromArchiveAsync(File file) {
        return CompletableFuture.supplyAsync(() -> loadExamFromArchive(file));
    }
}
