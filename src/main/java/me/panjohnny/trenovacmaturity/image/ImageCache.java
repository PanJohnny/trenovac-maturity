package me.panjohnny.trenovacmaturity.image;

import javafx.scene.image.Image;

import java.io.File;
import java.util.HashMap;

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
            File file = new File(key + ".png");
            if (file.exists()) {
                try {
                    cache.put(key, new Image(file.toURI().toString()));
                } catch (Exception e) {
                    cache.put(key, null);
                    return null;
                }
            } else {
                cache.put(key, null);
                return null;
            }
        }
        return cache.get(key);
    }


}
