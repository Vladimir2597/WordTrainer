package com.vladimir.wordtrainer.model;

import java.util.List;

public class Dictionary {
    private String name;
    private List<Word> words;

    public Dictionary(List<Word> words, String name) {
        this.words = words;
        this.name = name;
    }

    public void setWord(List<Word> words){
        this.words = words;
    }

    public Word getWord(int index){
        return words.get(index);
    }

    public String getName(){
        return name;
    }

    public int getCountWords() {
        return words.size();
    }
}
