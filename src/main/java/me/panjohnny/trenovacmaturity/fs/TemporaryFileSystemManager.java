package me.panjohnny.trenovacmaturity.fs;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TemporaryFileSystemManager {
    public static final Path TEMP_FOLDER = Path.of("generated/temp/");

    public static void cleanup() throws IOException {
        if (Files.exists(TEMP_FOLDER)) {
            try (var stream = Files.walk(TEMP_FOLDER)) {
                stream.map(Path::toFile)
                        .sorted((o1, o2) -> -o1.compareTo(o2)) // delete files before directories
                        .forEach(File::delete);
            }
        }
        Files.createDirectories(TEMP_FOLDER);
    }

    public static void ensureTempFolderExists() throws IOException {
        if (!Files.exists(TEMP_FOLDER)) {
            Files.createDirectories(TEMP_FOLDER);
        }
    }

    public static void writeBytes(String fileName, byte[] data) throws IOException {
        ensureTempFolderExists();
        Path imagePath = TEMP_FOLDER.resolve(fileName);
        Files.write(imagePath, data);
    }

    public static void writeImageRegion(String regionName, BufferedImage image) throws IOException {
        ensureTempFolderExists();
        Path imagePath = TEMP_FOLDER.resolve(regionName + ".png");
        ImageIO.write(image, "png", imagePath.toFile());
    }

    public static Path resolveRegionPath(String regionName) {
        return TEMP_FOLDER.resolve(regionName + ".png");
    }
}
