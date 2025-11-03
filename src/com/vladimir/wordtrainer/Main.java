package com.vladimir.wordtrainer;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.service.DictionaryManager;
import com.vladimir.wordtrainer.service.Trainer;

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

        Trainer trainer = new Trainer();
        trainer.start(dictionary);
    }
}