package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.util.WordUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public abstract class AbstractWordTrainer implements Trainer {
    private final Scanner scanner = new Scanner(System.in);
    private List<Integer> correctWords = new ArrayList<>();
    private List<Integer> usedWords = new ArrayList<>();
    private Dictionary dictionary;

    public AbstractWordTrainer(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    @Override
    public void start() {
        System.out.println("=== Word Trainer ===");
        System.out.println("Введите перевод слова. 'back' — Назад к выбору словаря.\n");

        while (true) {
            System.out.println("Осталось слов: " + getCountUnusedWord() +
                    " из " + dictionary.getCountWords() + " слов.");
            Integer wordIndex = getNewRandomWord();

            if (wordIndex != null) {
                Word word = dictionary.getWord(wordIndex);
                askQuestion(word);

                String answer = scanner.nextLine().trim();

                if (answer.equalsIgnoreCase("back")) {
                    System.out.println("До встречи!");
                    break;
                }

                if (isAnswerCorrect(answer, word)) correctWords.add(wordIndex);

                if (getCountUnusedWord() == 0) {
                    System.out.println("Поздравляю ты ответил: " + correctWords.size() +
                            " из " + dictionary.getCountWords() + " слов.");
                }
            } else {
                System.out.println("♻️ Все слова были использованы.");
                System.out.println("Что делаем:");
                System.out.println("1 - Повторим слова, на которые ты ответил неправильно?");
                System.out.println("2 - Повторим всё заново?");
                System.out.println("'back' - Назад, для выбора другого раздела.");

                String answer = scanner.nextLine().trim();

                if (answer.equalsIgnoreCase("back")) break;

                while (true) {
                    if (answer.equals("1")) {
                        usedWords = new ArrayList<>(correctWords);
                        break;
                    } else if (answer.equals("2")) {
                        usedWords.clear();
                        correctWords.clear();
                        break;
                    } else {
                        System.out.println("Нет такой команды, повторите ввод!");
                    }
                }
            }
        }
    }

    protected boolean isAnswerCorrect(String answer, Word word) {
        boolean result;

        if (WordUtil.equalsIgnorePrepositions(answer, word.getEnglish())) {
            System.out.println("✅ Правильно!\n");
            result = true;
        } else {
            System.out.println("❌ Неправильно! Правильный ответ: " + word.getEnglish() + "\n");
            result = false;
        }

        return result;
    }

    private Integer getNewRandomWord() {
        if (!usedWords.isEmpty() && usedWords.size() == dictionary.getCountWords()) {
            usedWords.clear();
            return null;
        }

        Random random = new Random();
        int unusedWordIndex;

        do {
            unusedWordIndex = random.nextInt(dictionary.getCountWords());
        } while (usedWords.contains(unusedWordIndex));

        usedWords.add(unusedWordIndex);

        return unusedWordIndex;
    }

    public int getCountUnusedWord() {
        return dictionary.getCountWords() - usedWords.size();
    }

    protected abstract void askQuestion(Word word);
}
