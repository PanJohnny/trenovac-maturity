package me.panjohnny.trenovacmaturity.model.answer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import javafx.scene.image.Image;
import me.panjohnny.trenovacmaturity.image.ImageCache;
import me.panjohnny.trenovacmaturity.model.JsonSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record Answer(int number, String text, Image image, String region_id) implements JsonSerializable {
    @Override
    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("number", number);
        object.addProperty("text", text);
        object.addProperty("region_id", region_id);

        return object;
    }

    public Answer createCopy(int newNumber) {
        return new Answer(newNumber, text, image, region_id);
    }

    public static Answer deserialize(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        int num = object.get("number").getAsInt();
        String text = object.get("text").getAsString();
        String region_id = object.get("region_id").getAsString();

        return new Answer(num, text, ImageCache.getInstance().getImage(region_id), region_id);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        Answer answer = (Answer) o;
        return number == answer.number && Objects.equals(text, answer.text) && Objects.equals(region_id, answer.region_id);
    }

    @Override
    public int hashCode() {
        int result = number;
        result = 31 * result + Objects.hashCode(text);
        result = 31 * result + Objects.hashCode(region_id);
        return result;
    }

    @Override
    @NotNull
    public String toString() {
        return "Answer{" +
                "number=" + number +
                ", text='" + text + '\'' +
                ", region_id='" + region_id + '\'' +
                '}';
    }
}
