package me.panjohnny.trenovacmaturity.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.scene.image.Image;
import me.panjohnny.trenovacmaturity.image.ImageCache;

import java.util.ArrayList;
import java.util.List;

public record Question(int number, String text, Image image, String region_id, List<String> tags) implements JsonSerializable {
    public Question(int number, String text, Image image, String region_id) {
        this(number, text, image, region_id, new ArrayList<>());
    }

    public String getTagString() {
        if (tags.isEmpty())
            return "";
        StringBuilder sb = new StringBuilder();
        for (String tag : tags) {
            sb.append(tag).append(", ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    public Question createCopy(int newNumber) {
        return new Question(newNumber, text, image, region_id, tags);
    }

    public void setTags(String s) {
        String[] questions = s.split(",");
        tags.clear();
        for (String question : questions) {
            tags.add(question.trim());
        }
    }

    public boolean isTagged() {
        return !getTagString().isEmpty();
    }

    public boolean isTagged(String tag) {
        return tags.contains(tag);
    }

    /** checks if it is tagged with atleast one tag */
    public boolean isNotTaggedWithAny(List<String> tags) {
        for (String tag : tags) {
            if (isTagged(tag)) return false;
        }
        return true;
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
    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("number", number);
        object.addProperty("text", text);
        object.addProperty("region_id", region_id);

        JsonArray array = new JsonArray();

        for (String tag : tags) {
            array.add(tag);
        }
        
        object.add("tags", array);

        return object;
    }

    public static Question deserialize(JsonElement element) {
        JsonObject object = element.getAsJsonObject();

        int number = object.get("number").getAsInt();
        String text = object.get("text").getAsString();
        String region_id = object.get("region_id").getAsString();

        JsonArray tagArray = object.get("tags").getAsJsonArray();

        List<String> tags = new ArrayList<>(tagArray.asList().stream().map(JsonElement::getAsString).toList());

        return new Question(number, text, ImageCache.getInstance().getImage(region_id), region_id, tags);
    }
}
