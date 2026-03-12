package me.panjohnny.trenovacmaturity.model;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.List;

public class Training extends Exam {
    private QuestionAnswerMap qaMap;
    private AnswerSet answers;
    public Training(String meta) {
        super(meta);
        qaMap = new QuestionAnswerMap();
        answers = new AnswerSet(meta + " answers");
    }

    @Override
    public JsonElement serialize() {
        JsonObject jsonObject = super.serialize().getAsJsonObject();
        jsonObject.addProperty("meta", getMeta());
        jsonObject.addProperty("training", "true");

        JsonArray questions = new JsonArray();
        for (Question q : this) {
            questions.add(q.serialize());
        }

        jsonObject.add("questions", questions);
        jsonObject.add("answers", answers.serialize());
        jsonObject.add("qamap",  qaMap.serialize());

        return jsonObject;
    }

    public void setQaMap(QuestionAnswerMap qaMap) {
        this.qaMap = qaMap;
    }

    public void setAnswers(AnswerSet answers) {
        this.answers = answers;
    }

    public static Training deserialize(JsonElement element) {
        JsonObject object = element.getAsJsonObject();
        if (!object.has("training") || !object.get("training").getAsBoolean()) {
            throw new IllegalArgumentException("Provided JSON is not a training");
        }

        String meta = object.get("meta").getAsString();
        Training training = new Training(meta);

        for (JsonElement questionElement : object.getAsJsonArray("questions")) {
            training.add(Question.deserialize(questionElement));
        }

        AnswerSet answers = AnswerSet.deserialize(object.get("answers"));
        training.setAnswers(answers);
        training.setQaMap(QuestionAnswerMap.deserialize(object.get("qamap"), training, answers));

        return training;
    }

    public void add(Question q, List<Answer> a) {
        qaMap.put(q, a);
        add(q);
        answers.addAll(a);
    }

    public QuestionAnswerMap getQaMap() {
        return qaMap;
    }

    public AnswerSet getAnswers() {
        return answers;
    }
}
