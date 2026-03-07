package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;

public class RusToEngTrainer extends AbstractWordTrainer {
    public RusToEngTrainer(Dictionary dictionary) {
        super(dictionary);
    }

    @Override
    protected String formatQuestion(Word word) {
        return word.getRussian() + "\n\nВведите слово на английском:";
    }
}