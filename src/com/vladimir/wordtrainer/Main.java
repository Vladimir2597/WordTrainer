package com.vladimir.wordtrainer;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.service.DictionaryManager;
import com.vladimir.wordtrainer.service.DefinitionTrainer;
import com.vladimir.wordtrainer.service.Trainer;
import com.vladimir.wordtrainer.service.RusToEngTrainer;

import java.util.Scanner;

public class Main {
     static void main(String[] args) {
        DictionaryManager dictionaryManager = new DictionaryManager("dictionaries.txt");
        Scanner scanner = new Scanner(System.in);

        while(true) {
            dictionaryManager.printList();
            System.out.print("\nВыберите номер словар. Для выхода напишите 'exit': ");

            String input = scanner.nextLine().trim();
            if (input.equals("exit")) return;

            int index;
            try {
                index = Integer.parseInt(input) - 1;
            } catch (NumberFormatException e) {
                System.out.println("⚠️ Введите номер.");
                continue;
            }

            Dictionary dictionary = dictionaryManager.loadDictionaryByIndex(index);
            if (dictionary == null) {
                System.out.println("⚠️ Неверный выбор.");
                continue; // Снова к списку словарей
            }

            Trainer trainer = null;
            while (true) {
                System.out.println("Вы выбрали: " + dictionary.getName());

                System.out.println("\nДоступные варианты изучения слов:");
                System.out.println("1 — По определению на английском");
                System.out.println("2 — По слову на русском");
                System.out.println("'back' — Назад к выбору словаря");
                System.out.print("\nВыберите вариант изучения слов: ");

                String mode = scanner.nextLine().trim();
                if (mode.equals("back")) {
                    break; // назад к внешнему while -> выбор словаря
                }

                switch (mode) {
                    case "1" -> trainer = new DefinitionTrainer(dictionary);
                    case "2" -> trainer = new RusToEngTrainer(dictionary);
                    default -> {
                        System.out.println("Такой команды нет");
                        continue;
                    }
                }
                break;
            }

            trainer.start();
        }
    }
}