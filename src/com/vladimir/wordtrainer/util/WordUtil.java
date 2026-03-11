package com.vladimir.wordtrainer.util;

import java.util.Set;
import java.util.StringTokenizer;

public class WordUtil {
    private static final Set<String> IGNORE_WORDS = Set.of(
            "a", "an", "the", "to"
    );

    private static String normalize(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        String normalizedInput = input.replaceAll("[()]", "").trim().toLowerCase();
        String[] wordPart = normalizedInput.split("\\s+", 2);

        if (wordPart.length == 2 && IGNORE_WORDS.contains(wordPart[0])){
            return wordPart[1];
        }

        return normalizedInput;
    }

    public static boolean equalsIgnorePrepositions(String s1, String s2) {
        return normalize(s1).equals(normalize(s2));
    }
}
