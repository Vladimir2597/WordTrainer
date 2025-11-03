package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;

import java.util.Scanner;

public class Trainer {
    private final Scanner scanner = new Scanner(System.in);

    public void start(Dictionary dictionary) {
        System.out.println("=== Word Trainer ===");
        System.out.println("Введите перевод слова. Для выхода напишите 'exit'.\n");

        while (true) {
            Word word = dictionary.getRandomWord();
            System.out.print(word.getEnglishDescription() + " → ");
            String answer = scanner.nextLine().trim();

            if (answer.equalsIgnoreCase("exit")) {
                System.out.println("До встречи!");
                break;
            }

            if (answer.equalsIgnoreCase(word.getEnglish())) {
                System.out.println("✅ Правильно!\n");
            } else {
                System.out.println("❌ Неправильно! Правильный ответ: " + word.getEnglish() + "\n");
            }
        }
    }
}
