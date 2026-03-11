package com.vladimir.wordtrainer.session;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.service.AbstractWordTrainer;

public class UserSession {
    private AppState state = AppState.CHOOSING_DICTIONARY;
    private Dictionary dictionary;
    private AbstractWordTrainer trainer;

    public AppState getState() {
        return state;
    }

    public void setState(AppState state) {
        this.state = state;
    }

    public Dictionary getDictionary() {
        return dictionary;
    }

    public void setDictionary(Dictionary dictionary) {
        this.dictionary = dictionary;
    }

    public AbstractWordTrainer getTrainer() {
        return trainer;
    }

    public void setTrainer(AbstractWordTrainer trainer) {
        this.trainer = trainer;
    }
}
