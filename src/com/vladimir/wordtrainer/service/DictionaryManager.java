package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.db.DictionaryRepository;
import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictionaryManager {
    private final DictionaryRepository dictionaryRepository;
    private List<Long> ids;
    private List<String> names;

    public DictionaryManager(DictionaryRepository dictionaryRepository) {
        this.dictionaryRepository = dictionaryRepository;
        reload();
    }

    private void reload() {
        Map<Long, String> map = dictionaryRepository.getAllDictionaries();
        ids = new ArrayList<>(map.keySet());
        names = new ArrayList<>(map.values());
    }

    public List<String> getNames() {
        return names;
    }

    public Dictionary loadDictionaryByIndex(int index) {
        long dictionaryId = ids.get(index);
        String name = names.get(index);
        List<Word> words = dictionaryRepository.getWordsByDictionaryId(dictionaryId);
        return new Dictionary(words, name);
    }
}