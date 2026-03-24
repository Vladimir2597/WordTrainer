package com.vladimir.wordtrainer.util;

import com.vladimir.wordtrainer.model.Word;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

public class FileUtil {
    public static String wrapText(String text, int width) {
        if (text == null) return "";
        StringBuilder sb = new StringBuilder();
        int lineLength = 0;

        for (String word : text.split(" ")) {
            if (lineLength + word.length() > width) {
                sb.append("\n");
                lineLength = 0;
            }
            sb.append(word).append(" ");
            lineLength += word.length() + 1;
        }

        return sb.toString().trim();
    }

    public static String loadDictionaryName(File file) {
        try {
            List<String> lines = Files.readAllLines(file.toPath());
            if (!lines.isEmpty()) return lines.get(0).trim();
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + file.getName() + " — " + e.getMessage());
        }
        return null;
    }

    public static List<Word> loadWordsFromFile(File file) {
        List<Word> words = new ArrayList<>();

        try {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int i = 1; i < lines.size(); i++) {
                String line = lines.get(i).trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("=", 3);
                if (parts.length == 3) {
                    words.add(new Word(null, parts[0].trim(), parts[1].trim(), wrapText(parts[2].trim(), 80)));
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + file.getName() + " — " + e.getMessage());
        }

        return words;
    }
}