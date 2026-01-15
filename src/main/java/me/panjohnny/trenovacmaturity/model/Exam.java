package me.panjohnny.trenovacmaturity.model;

import java.util.List;

public class Exam {
    private final List<Question> questions;

    private int currentQuestionIndex = 0;
    public Exam(List<Question> questions) {
        this.questions = questions;
    }

    public void nextQuestion() {
        if (currentQuestionIndex < questions.size() - 1) {
            currentQuestionIndex++;
        }
    }

    public void previousQuestion() {
        if (currentQuestionIndex > 0) {
            currentQuestionIndex--;
        }
    }

    public Question getCurrentQuestion() {
        return questions.get(currentQuestionIndex);
    }

    public String serialize() {
        StringBuilder sb = new StringBuilder();
        for (Question q : questions) {
            sb.append(q.serialize()).append("\n");
        }
        return sb.toString();
    }

    public static Exam deserialize(String data) {
        String[] lines = data.split("\n");
        List<Question> questions = new java.util.ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                questions.add(Question.deserialize(line));
            }
        }
        return new Exam(questions);
    }
}
