package com.vladimir.wordtrainer.util;

import com.vladimir.wordtrainer.model.Word;

import java.io.*;
import java.util.*;

public class FileUtil {
    private static List<String> readLines(String filePath) {
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty()) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.err.println("Ошибка чтения файла: " + filePath + " — " + e.getMessage());
        }
        return lines;
    }

    public static Map<String, String> loadDictionaries(String filePath) {
        Map<String, String> config = new LinkedHashMap<>();
        for (String line : readLines(filePath)) {
            if (line.startsWith("#")) continue;
            String[] parts = line.split("=");
            if (parts.length == 2) {
                config.put(parts[0].trim(), parts[1].trim());
            }
        }
        return config;
    }

    public static List<Word> loadWords(String filePath){
        List<Word> words = new ArrayList<>();
        for (String line : readLines(filePath)) {
            if (line.startsWith("#")) continue;
            String[] parts = line.split("=");
            if (parts.length == 3) {
                words.add(new Word(parts[0].trim(), parts[1].trim(), wrapText(parts[2].trim(), 80)));
            }
        }
        return words;
    }

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
}