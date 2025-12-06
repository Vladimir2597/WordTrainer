
package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;

public class DefinitionTrainer extends AbstractWordTrainer {
    public DefinitionTrainer(Dictionary dictionary) {
        super(dictionary);
    }

    @Override
    protected void askQuestion(Word word) {
        System.out.print(word.getEnglishDescription() + " → ");
    }
}
