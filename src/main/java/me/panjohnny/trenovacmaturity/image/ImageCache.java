package me.panjohnny.trenovacmaturity.image;

import javafx.scene.image.Image;
import me.panjohnny.trenovacmaturity.fs.TemporaryFileSystemManager;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;

/**
 * Loading cache for images stored on disk.
 */
public class ImageCache {
    private final HashMap<String, Image> cache = new HashMap<>();

    private static ImageCache self;

    public static ImageCache getInstance() {
        if (self == null) {
            self = new ImageCache();
        }
        return self;
    }

    public Image getImage(String key) {
        if (!cache.containsKey(key)) {
            // load image from disk if exists
            Path path = TemporaryFileSystemManager.resolveRegionPath(key);
            System.out.println("Loading image from path: " + path.toString());
            if (Files.exists(path)) {
                try {
                    cache.put(key, new Image(path.toUri().toString()));
                } catch (Exception e) {
                    cache.put(key, null);
                    System.out.println("Failed to load image from path: " + path.toString());
                    return null;
                }
            } else {
                cache.put(key, null);
                return null;
            }
        }
        return cache.get(key);
    }

    public void clear() {
        cache.clear();
    }
}
