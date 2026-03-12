package me.panjohnny.trenovacmaturity.fx.training;

import me.panjohnny.trenovacmaturity.fx.InExamController;
import me.panjohnny.trenovacmaturity.model.Question;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TrainingPanicController extends InExamController {
    List<Integer> order;
    int index = 0;
    @Override
    public void loadAppData() {
        super.loadAppData();

        ArrayList<Question> copy = new ArrayList<>(this.exam);
        Collections.shuffle(copy);

        exam.setCurrentQuestionIndex(copy.getFirst().number() - 1);
        redraw();
        this.timeLimitSeconds = training.getCurrentQuestion().text().length();
        startTimerCount();

        order = new ArrayList<>();

        for (Question question : copy) {
            order.add(question.number());
        }
    }

    @Override
    protected void onNextClick() {
        index++;
        int realIndex = order.get(index) - 1;
        exam.setCurrentQuestionIndex(realIndex);

        redraw();
        this.timeLimitSeconds = training.getCurrentQuestion().text().length();
        startTimerCount();
    }
}
