package com.vladimir.wordtrainer.util;

import java.util.Set;
import java.util.StringTokenizer;

public class WordUtil {
    private static final Set<String> IGNORE_WORDS = Set.of(
            "a", "an", "the", "to"
    );

    private static String normalize(String input) {
        StringTokenizer stringTokenizer = new StringTokenizer(input);

        if (input == null || input == "") return "";
        else if (stringTokenizer.countTokens() <= 1) return input.toLowerCase().trim();
        else {
            input = input.trim().toLowerCase();

            return IGNORE_WORDS.contains(stringTokenizer.nextToken().toString()) ?
                    input.substring(input.indexOf(' '), input.length()).trim() :
                    input;
        }
    }

    public static boolean equalsIgnorePrepositions(String s1, String s2) {
        return normalize(s1).equals(normalize(s2));
    }
}
