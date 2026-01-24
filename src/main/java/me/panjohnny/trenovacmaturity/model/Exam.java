package me.panjohnny.trenovacmaturity.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.List;

public class Exam extends ArrayList<Question> implements JsonSerializable {

    private int currentQuestionIndex = 0;
    private final String meta;

    public Exam(String meta) {
        this.meta = meta;
    }

    public void nextQuestion() {
        if (currentQuestionIndex < this.size() - 1) {
            currentQuestionIndex++;
        }
    }

    public void previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
        }
    }

    public Question getCurrentQuestion() {
        return this.get(currentQuestionIndex);
    }

    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        JsonArray array = new JsonArray();

        for (Question q : this) {
            array.add(q.serialize());
        }

        object.addProperty("meta", this.meta);
        object.add("questions", array);

        return object;
    }

    public static Exam deserialize(JsonElement data) {
        JsonObject object = data.getAsJsonObject();
        JsonArray array = object.getAsJsonArray("questions");

        Exam exam = new Exam(object.get("meta").getAsString());

        for (JsonElement el : array) {
            exam.add(Question.deserialize(el));
        }

        return exam;
    }

    public int length() {
        return this.size();
    }

    public String getMeta() {
        return meta;
    }
}
