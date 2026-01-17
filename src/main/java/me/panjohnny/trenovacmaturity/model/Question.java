package me.panjohnny.trenovacmaturity.model;

import javafx.scene.image.Image;
import me.panjohnny.trenovacmaturity.image.ImageCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public record Question(int number, String text, Image image, String region_id, List<String> tags) {
    public Question(int number, String text, Image image, String region_id) {
        this(number, text, image, region_id, new ArrayList<>());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Question question = (Question) o;
        return number == question.number && text.equals(question.text) && region_id.equals(question.region_id) && tags.equals(question.tags);
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + text.hashCode();
        result = 31 * result + region_id.hashCode();
        result = 31 * result + tags.hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Question{" +
                "number=" + number +
                ", text='" + text + '\'' +
                ", region_id='" + region_id + '\'' +
                ", tags=" + tags +
                '}';
    }

    public String serialize() {
        return number + "|" + text.replace("\n", "\\n").replace("|", "\\|") + "|" + region_id + "|" + String.join(",", tags);
    }

    public static Question deserialize(String line) {
        String[] parts = line.split("(?<!\\\\)\\|", 4);
        int number = Integer.parseInt(parts[0]);
        String text = parts[1].replace("\\n", "\n").replace("\\|", "|");
        String region_id = parts[2];
        List<String> tags = new ArrayList<>();
        if (parts.length > 3 && !parts[3].isEmpty()) {
            String[] tagParts = parts[3].split(",");
            Collections.addAll(tags, tagParts);
        }
        return new Question(number, text, ImageCache.getInstance().getImage(region_id), region_id, tags);
    }
}
