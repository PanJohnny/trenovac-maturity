package me.panjohnny.trenovacmaturity.archive;

import me.panjohnny.trenovacmaturity.image.ImageCache;
import me.panjohnny.trenovacmaturity.model.Exam;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveLoader {
    public static Exam loadExamFromArchive(File file) {
        try(ZipInputStream inputStream = new ZipInputStream(new FileInputStream(file))) {
            ZipEntry entry;
            String examString = null;
            while ((entry = inputStream.getNextEntry()) != null) {
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

            return Exam.deserialize(examString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
