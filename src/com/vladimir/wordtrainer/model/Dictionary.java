package com.vladimir.wordtrainer.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Dictionary {
    private List<Integer> usedWords = new ArrayList<>();
    private String name;
    private List<Word> words;

    public Dictionary(List<Word> words, String name) {
        this.words = words;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public Word getRandomWord() {
        if (!usedWords.isEmpty() && usedWords.size() == words.size()) {
            usedWords.clear();
            System.out.println("♻️ Все слова были использованы, начинаем заново.");
        }

        Random random = new Random();
        int unusedWordIndex;

        do {
            unusedWordIndex = random.nextInt(words.size());
        } while (usedWords.contains(unusedWordIndex));

        usedWords.add(unusedWordIndex);
        return words.get(unusedWordIndex);
    }
}
