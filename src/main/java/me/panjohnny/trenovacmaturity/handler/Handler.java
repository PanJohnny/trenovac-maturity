package me.panjohnny.trenovacmaturity.handler;

import me.panjohnny.trenovacmaturity.MaturitaApplication;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;
import me.panjohnny.trenovacmaturity.model.answer.AnswerSet;
import me.panjohnny.trenovacmaturity.model.training.Training;

import java.io.File;
import java.io.IOException;

public abstract class Handler {
    protected MaturitaApplication application;

    /*
     globals
     */
    protected Exam exam;
    protected QuestionAnswerMap questionAnswerMap;
    protected AnswerSet answers;
    protected Training training;
    protected File archivePath;

    public Handler(MaturitaApplication application) {
        this.application = application;
    }

    /**
     * Used to free resources no longer in use
     */
    protected abstract void freeNonDefaults();

    public abstract void save() throws IOException;

    public void free() {
        this.exam = null;
        this.questionAnswerMap = null;
        this.answers = null;
        this.training = null;
        this.archivePath = null;

        this.freeNonDefaults();
    }

    public Exam getExam() {
        return exam;
    }

    public QuestionAnswerMap getQuestionAnswerMap() {
        return questionAnswerMap;
    }

    public AnswerSet getAnswers() {
        return answers;
    }

    public Training getTraining() {
        return training;
    }

    public File getArchivePath() {
        return archivePath;
    }
}
