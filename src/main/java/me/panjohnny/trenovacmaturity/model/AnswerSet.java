package me.panjohnny.trenovacmaturity.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class AnswerSet extends ArrayList<Answer> implements JsonSerializable {
    private final String meta;
    public AnswerSet(String meta) {
        super();
        this.meta = meta;
    }

    @Override
    public JsonElement serialize() {
        JsonObject object = new JsonObject();
        object.addProperty("meta", meta);

        JsonArray array = new JsonArray();

        for (Answer answer : this) {
            array.add(answer.serialize());
        }

        object.add("answers", array);

        return object;
    }

    public static AnswerSet deserialize(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        JsonArray array = object.getAsJsonArray("answers");
        String meta = object.get("meta").getAsString();

        AnswerSet answerSet = new AnswerSet(meta);

        for (JsonElement el : array) {
            answerSet.add(Answer.deserialize(el));
        }

        answerSet.sort(Comparator.comparingInt(Answer::number));

        return answerSet;
    }

    public String getMeta() {
        return meta;
    }

    public QuestionAnswerMap autoAssign(Exam exam) {
        QuestionAnswerMap map = new QuestionAnswerMap();
        for (int i = 0; i < exam.length(); i++) {
            Question question = exam.get(i);
            Answer answer = this.get(i);

            // detect stuff like "ÚLOHÁM 19–20"
            int indexOfUlo = question.text().indexOf("ÚLOHÁM");
            if (indexOfUlo != -1) {
                String substr = question.text().substring(indexOfUlo + 7).trim();
                substr = substr.substring(0, substr.indexOf(' ')).trim();
                String[] nums = substr.split("–");
                if (nums.length == 2) {
                    try {
                        int from = Integer.parseInt(nums[0]);
                        int to = Integer.parseInt(nums[1]);

                        List<Answer> answers = new ArrayList<>();
                        for (int a = from; a < to; a++) {
                            answers.add(get(a - 1));
                        }

                        i = to;
                        map.put(question, answers);
                        continue;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            // just add one
            map.put(question, answer);
        }

        return map;
    }
}
