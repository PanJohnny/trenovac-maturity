package me.panjohnny.trenovacmaturity.fs;

import me.panjohnny.trenovacmaturity.model.AnswerSet;
import me.panjohnny.trenovacmaturity.model.Exam;
import me.panjohnny.trenovacmaturity.model.QuestionAnswerMap;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public record MaturitaFile(Path path, Exam exam, @Nullable AnswerSet answerSet, @Nullable QuestionAnswerMap qaMap) {
}