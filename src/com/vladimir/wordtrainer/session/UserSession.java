package com.vladimir.wordtrainer.session;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.service.trainer.Trainer;
import com.vladimir.wordtrainer.service.trainer.Trainer;

import java.util.List;
import java.util.Map;

public class UserSession {
    private AppState state = AppState.CHOOSING_DICTIONARY;
    private Dictionary dictionary;
    private Trainer trainer;
    private long telegramUserId;
    private String pendingDictionaryName;
    private List<Word> pendingWords;
    private boolean isExistsInRepository;
    private Map<Long, String> availableDictionary;

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

    public Trainer getTrainer() {
        return trainer;
    }

    public void setTrainer(Trainer trainer) {
        this.trainer = trainer;
    }

    public long getTelegramUserId() {
        return telegramUserId;
    }

    public void setTelegramUserId(long telegramUserId) {
        this.telegramUserId = telegramUserId;
    }

    public String getPendingDictionaryName(){ return pendingDictionaryName; }

    public void setPendingDictionaryName(String pendingDictionaryName) { this.pendingDictionaryName = pendingDictionaryName; }

    public List<Word> getPendingWords(){ return pendingWords; }

    public void setPendingWords(List<Word> pendingWords) { this.pendingWords = pendingWords; }

    public void setExistsInRepository(boolean existsInRepository) {
        isExistsInRepository = existsInRepository;
    }

    public boolean isExistsInRepository() {
        return isExistsInRepository;
    }

    public Map<Long, String> getAvailableDictionary() {
        return availableDictionary;
    }

    public void setAvailableDictionary(Map<Long, String> availableDictionary) {
        this.availableDictionary = availableDictionary;
    }
}