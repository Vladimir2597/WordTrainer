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

        dictionaryManager.printList();

        Scanner scanner = new Scanner(System.in);
        System.out.print("\nВыберите номер словаря: ");
        int index = scanner.nextInt() - 1;

        Dictionary dictionary = dictionaryManager.loadDictionaryByIndex(index);
        if (dictionary == null) {
            System.out.println("⚠️ Неверный выбор.");
            return;
        }

        System.out.println("Вы выбрали: " + dictionary.getName());

        System.out.println("\nДоступные варианты изучения слов:");
        System.out.println("1 — По определению на английском");
        System.out.println("2 — На основе русского слова");
        System.out.print("\nВыберите вариант изучения слов: ");

        Trainer trainer;

        while (true) {
            int choice = scanner.nextInt();
            switch (choice) {
                case 1:
                    trainer = new DefinitionTrainer(dictionary);
                    break;
                case 2:
                    trainer = new RusToEngTrainer(dictionary);
                    break;
                default:
                    System.out.println("Такой команды нет");
                    continue;
            }

            break;
        }

        trainer.start();
    }
}