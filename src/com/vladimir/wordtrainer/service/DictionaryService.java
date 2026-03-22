package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.db.DictionaryRepository;
import com.vladimir.wordtrainer.db.UserDictionaryRepository;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.util.FileUtil;

import java.io.File;
import java.util.List;
import java.util.Map;

public class DictionaryService {
    private final DictionaryRepository dictionaryRepository;
    private final UserDictionaryRepository userDictionaryRepository;

    public DictionaryService(DictionaryRepository dictionaryRepository,
                             UserDictionaryRepository userDictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
        this.userDictionaryRepository = userDictionaryRepository;
    }

    public Map<Long, String> getUserSelectedDictionaries(long telegramUserId) {
        return dictionaryRepository.getUserSelectedDictionaries(telegramUserId);
    }

    public Map<Long, String> getDictionariesToAdd(long telegramUserId) {
        return dictionaryRepository.getDictionariesToAdd(telegramUserId);
    }

    public Map<Long, String> getDictionariesToRemove(long telegramUserId) {
        return dictionaryRepository.getDictionariesToRemove(telegramUserId);
    }

    public Map<Long, String> getOwnDictionaries(long telegramUserId) {
        return dictionaryRepository.getOwnDictionaries(telegramUserId);
    }

    public List<Word> getWords(long dictionaryId) {
        return dictionaryRepository.getWordsByDictionaryId(dictionaryId);
    }

    public long saveDictionary(String name, long telegramUserId, List<Word> words, boolean isPublic) {
        long dictionaryId = dictionaryRepository.saveDictionary(name, telegramUserId, isPublic);
        dictionaryRepository.saveWord(dictionaryId, words);
        return dictionaryId;
    }

    public void addDictionaryToUser(long telegramUserId, long dictionaryId) {
        userDictionaryRepository.addDictionaryToUser(telegramUserId, dictionaryId);
    }

    public String parseDictionaryName(File file) {
        return FileUtil.loadDictionaryName(file);
    }

    public List<Word> parseDictionaryWords(File file) {
        return FileUtil.loadWordsFromFile(file);
    }

    public void removeDictionaryFromUser(long telegramUserId, long dictionaryId){
        userDictionaryRepository.removeDictionaryFromUser(telegramUserId, dictionaryId);
    }

    public void deleteDictionary(long dictionaryId){
        dictionaryRepository.deleteDictionary(dictionaryId);
    }
}