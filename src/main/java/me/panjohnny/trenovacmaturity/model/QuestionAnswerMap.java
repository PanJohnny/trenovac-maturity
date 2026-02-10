package me.panjohnny.trenovacmaturity.model;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import me.panjohnny.trenovacmaturity.ExceptionHandler;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

public class QuestionAnswerMap extends LinkedHashMap<Question, List<Answer>> implements JsonSerializable {
    public void put(Question q, Answer... answer) {
        super.put(q, List.of(answer));
    }

    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        // schena: question-number: answer-number[]
        for (var entry : this.entrySet()) {
            List<Answer> answers = entry.getValue();
            JsonArray array = new JsonArray();

            for (Answer answer : answers) {
                array.add(answer.number());
            }

            object.add(String.valueOf(entry.getKey().number()), array);
        }

        return object;
    }

    public static QuestionAnswerMap deserialize(JsonElement data, Exam exam, AnswerSet answerSet) {
        QuestionAnswerMap map = new QuestionAnswerMap();

        JsonObject object = data.getAsJsonObject();

        for (var entry : object.entrySet()) {
            String numStr = entry.getKey();
            JsonArray ansArray = entry.getValue().getAsJsonArray();

            try {
                int questionNumber = Integer.parseInt(numStr);
                Question q = exam.get(questionNumber - 1);
                List<Answer> answers = new ArrayList<>();
                for (JsonElement el : ansArray) {
                    int answerNumber = el.getAsInt();
                    answers.add(answerSet.get(answerNumber - 1));
                }

                map.put(q, answers);
            } catch (Exception e) {
                ExceptionHandler.handleWarning(e, "Failed to deserialize question-answer map entry: " + numStr);
            }
        }

        return map;
    }


}
