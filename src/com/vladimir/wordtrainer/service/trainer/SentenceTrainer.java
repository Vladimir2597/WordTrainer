package com.vladimir.wordtrainer.service.trainer;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.SentenceEvaluation;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.service.AIService;

public class SentenceTrainer extends AbstractWordTrainer {
    private final AIService aiService;
    private static final int MIN_CORRECT_PERCENTAGE = 70;

    public SentenceTrainer(Dictionary dictionary, AIService aiService) {
        super(dictionary);
        this.aiService = aiService;
    }

    @Override
    protected String formatQuestion(Word word) {
        return "Составьте предложение со словом: " + word.getEnglish();
    }

    @Override
    public String handleAnswer(String sentence) {
        Word word = getCurrentWord();

        SentenceEvaluation evaluation = aiService.evaluate(word.getEnglish(), sentence);

        if (evaluation == null) {
            moveToNext(false);
            return "⚠️ Не удалось получить оценку от AI. Слово пропущено.";
        }

        boolean isCorrect = evaluation.getPercentage() >= MIN_CORRECT_PERCENTAGE;

        moveToNext(isCorrect);

        return (isCorrect ? "✅ " : "❌ ") +
                "Правильность: " + evaluation.getPercentage() + "%\n" +
                evaluation.getFeedback();

    }
}
