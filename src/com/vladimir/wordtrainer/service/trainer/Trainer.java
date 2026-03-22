package com.vladimir.wordtrainer.service.trainer;

import com.vladimir.wordtrainer.model.Word;

public interface Trainer {
    String handleAnswer(String answer);
    String getNextQuestion();
    boolean isFinished();
    void resetAll();
    void resetWithWrongOnly();
    String getResultText();
    boolean existsMoreWords();
    String getProgressText();
    Word getCurrentWord();
}