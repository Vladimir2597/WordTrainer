package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.util.WordUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractWordTrainer implements Trainer {
    private final Dictionary dictionary;
    private final List<Integer> shuffledIndices;
    private final List<Integer> correctIndices = new ArrayList<>();
    private int currentPosition = 0;
    private int numberRemainingWords;

    public AbstractWordTrainer(Dictionary dictionary) {
        this.dictionary = dictionary;
        this.numberRemainingWords = dictionary.getCountWords();
        this.shuffledIndices = new ArrayList<>();
        for (int i = 0; i < dictionary.getCountWords(); i++) {
            shuffledIndices.add(i);
        }
        Collections.shuffle(shuffledIndices);
    }

    public String getNextQuestion() {
        if (isFinished()) return null;
        Word word = dictionary.getWord(shuffledIndices.get(currentPosition));
        return formatQuestion(word);
    }

    public String handleAnswer(String answer) {
        if (isFinished()) return null;

        int wordIndex = shuffledIndices.get(currentPosition);
        Word word = dictionary.getWord(wordIndex);

        String response;
        if (WordUtil.equalsIgnorePrepositions(answer, word.getEnglish())) {
            correctIndices.add(wordIndex);
            response = "✅ Правильно!";
        } else {
            response = "❌ Неправильно! Правильный ответ: " + word.getEnglish();
        }

        currentPosition++;
        return response;
    }

    public boolean isFinished() {
        return currentPosition >= shuffledIndices.size();
    }

    public String getProgressText() {
        int remaining = shuffledIndices.size() - currentPosition;
        return "Осталось слов: " + remaining + " из " + numberRemainingWords;
    }

    public String getResultText() {
        return "Результат: " + correctIndices.size() + " из " + numberRemainingWords + " правильных ответов.";
    }

    public void resetWithWrongOnly() {
        List<Integer> wrongIndices = new ArrayList<>();
        for (int idx : shuffledIndices) {
            if (!correctIndices.contains(idx)) {
                wrongIndices.add(idx);
            }
        }
        shuffledIndices.clear();
        shuffledIndices.addAll(wrongIndices);
        Collections.shuffle(shuffledIndices);
        currentPosition = 0;
        numberRemainingWords = shuffledIndices.size();
        correctIndices.clear();
    }

    public void resetAll() {
        shuffledIndices.clear();
        for (int i = 0; i < dictionary.getCountWords(); i++) {
            shuffledIndices.add(i);
        }
        Collections.shuffle(shuffledIndices);
        currentPosition = 0;
        numberRemainingWords = shuffledIndices.size();
        correctIndices.clear();
    }

    public boolean existsMoreWords() {
        return correctIndices.size() < shuffledIndices.size();
    }

    protected abstract String formatQuestion(Word word);
}
