package me.panjohnny.trenovacmaturity;

import java.io.File;
import java.util.Properties;

public class RetentionHelper {
    private final Properties properties;
    private final File propertiesFile;

    public RetentionHelper(File path) {
        properties = new Properties();
        propertiesFile = path;
        try {
            if (path.exists()) {
                properties.load(new java.io.FileReader(path));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void put(String key, Object value) {
        properties.put(key, value);
        saveFile();
    }

    public String get(String key) {
        return properties.getProperty(key);
    }

    public String[] getArray(String key) {
        return (String[]) properties.get(key);
    }

    public void saveFile() {
        try {
            properties.store(new java.io.FileWriter(propertiesFile), "Retention Data for Trénovač Maturity");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
