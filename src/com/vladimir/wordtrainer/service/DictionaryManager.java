package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.util.FileUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictionaryManager {
    private List<String> names;
    private List<String> paths;
    private final static String RELATIVE_PATH = "src/com/vladimir/wordtrainer/data/";

    public DictionaryManager(String dictionaryFilmName){
        Map<String, String> map = FileUtil.loadDictionaries(RELATIVE_PATH + dictionaryFilmName);

        names = new ArrayList<>(map.keySet());
        paths = new ArrayList<>(map.values());
    }

    public List<String> getNames() {
        return names;
    }

    public Dictionary loadDictionaryByIndex(int index) {
        String name = names.get(index);
        String path = paths.get(index);

        List<Word> words = FileUtil.loadWords(RELATIVE_PATH + path);

        return new Dictionary(words, name);
    }
}
