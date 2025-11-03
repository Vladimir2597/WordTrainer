package com.vladimir.wordtrainer.model;

public class Word {
    private final String english;
    private final String russian;
    private final String englishDescription;

    public Word(String english, String russian, String englishDescription) {
        this.english = english;
        this.russian = russian;
        this.englishDescription = englishDescription;
    }

    public String getEnglish() {
        return english;
    }

    public String getRussian() {
        return russian;
    }

    public String getEnglishDescription() {
        return englishDescription;
    }

}
