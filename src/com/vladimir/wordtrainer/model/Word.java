package com.vladimir.wordtrainer.model;

public class Word {
    private final Long id;
    private final String english;
    private final String russian;
    private final String englishDescription;

    public Word(Long id, String english, String russian, String englishDescription) {
        this.id = id;
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

    public Long getId() {
        return id;
    }
}
