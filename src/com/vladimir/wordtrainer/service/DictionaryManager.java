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
    private String relativePath = "src/com/vladimir/wordtrainer/data/";

    public DictionaryManager(String dictionaryFilmName){
        Map<String, String> map = FileUtil.loadDictionaries(relativePath + dictionaryFilmName);

        names = new ArrayList<>(map.keySet());
        paths = new ArrayList<>(map.values());
    }

    public void printList(){
        if(names.isEmpty()){
            System.out.println("⚠️  Нет доступных словарей.");
            return;
        }

        System.out.println("\n📚 Доступные словари:");
        for(int i = 0; i < names.size(); i++){
            System.out.println((i + 1) + " — " + names.get(i));
        }
    }

    public Dictionary loadDictionaryByIndex(int index) {
        String name = names.get(index);
        String path = paths.get(index);

        List<Word> words = FileUtil.loadWords(relativePath + path);

        return new Dictionary(words, name);
    }
}
