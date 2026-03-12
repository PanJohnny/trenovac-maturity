package me.panjohnny.trenovacmaturity.model.training;

import me.panjohnny.trenovacmaturity.fs.MaturitaFile;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.Question;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;
import me.panjohnny.trenovacmaturity.model.answer.Answer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainingBuilder {
    private final List<MaturitaFile> maturitaFiles;
    private List<String> tagCache;
    private List<String> selectedTags;

    public TrainingBuilder(List<MaturitaFile> maturitaFiles) {
        this.maturitaFiles = maturitaFiles;
    }

    public void createTagCache() {
        tagCache = new ArrayList<>();

        for (MaturitaFile maturitaFile : maturitaFiles) {
            Exam exam = maturitaFile.exam();
            for (Question question : exam) {
                if (question.isTagged()) {
                    for (String tag : question.tags()) {
                        if (!tagCache.contains(tag)) {
                            tagCache.add(tag);
                        }
                    }
                }
            }
        }
    }

    public List<String> getTagCache() {
        return Collections.unmodifiableList(tagCache);
    }

    public void applyTagFilter(List<String> tags) {
        this.selectedTags = tags;
    }

    public Training create(String name) {
        Training training = new Training(name);
        int questionIndex = 1;
        int answerIndex = 1;

        boolean checkTags = selectedTags != null && !selectedTags.isEmpty();

        for (MaturitaFile maturitaFile : maturitaFiles) {
            QuestionAnswerMap map = maturitaFile.qaMap();
            if (map != null && !map.isEmpty()) {
                for (Question q : maturitaFile.exam()) {
                    if (checkTags && q.isNotTaggedWithAny(selectedTags)) {
                        continue;
                    }

                    Question copy = q.createCopy(questionIndex);
                    if (map.containsKey(q)) {
                        List<Answer> answers = map.get(q);
                        List<Answer> newList =  new ArrayList<>();

                        for (Answer answer : answers) {
                            newList.add(answer.createCopy(answerIndex));
                            answerIndex++;
                        }

                        training.add(copy, newList);
                    } else {
                        training.add(copy);
                    }
                    questionIndex++;
                }
            } else {
                for (Question q : maturitaFile.exam()) {
                    if (checkTags && q.isNotTaggedWithAny(selectedTags)) {
                        continue;
                    }

                    training.add(q.createCopy(questionIndex));

                    questionIndex++;
                }
            }
        }

        return training;
    }
}
