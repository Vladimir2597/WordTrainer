package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class AbstractWordTrainerTest {
    private static Dictionary buildDictionary(String... triples){
        List<Word> words = new ArrayList<>();

        for (int i = 0; i < triples.length; i += 3) {
            words.add(new Word(triples[i], triples[i+1], triples[i+2]));
        }

        return new Dictionary(words, "Test");
    }

    @Test
    void newTrainer_isNotFinished(){
        Dictionary dictionary = buildDictionary("word","translate","descriptions");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        assertFalse(trainer.isFinished());
    }

    @Test
    void getNextQuestion_returnsDescription(){
        Dictionary dictionary = buildDictionary("word","translate","a small animal");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        assertTrue(trainer.getNextQuestion().contains("a small animal"));
    }

    @Test
    void getNextQuestion_whenFinished_returnsNull(){
        Dictionary dictionary = buildDictionary("word","translate","a small animal");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);
        trainer.handleAnswer("a small animal");

        assertNull(trainer.getNextQuestion());
    }

    @Test
    void handleAnswer_correctAnswer_returnsSuccess(){
        Dictionary dictionary = buildDictionary("word","translate","a small animal");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        assertEquals("✅ Правильно!",
                        trainer.handleAnswer("word"));
    }

    @Test
    void handleAnswer_wrongAnswer_returnsFailureWithCorrectWord(){
        Dictionary dictionary = buildDictionary("word","translate","a small animal");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        assertTrue(trainer.handleAnswer("a small animal").contains("Неправильно! Правильный ответ"));
    }

    @Test
    void handleAnswer_whenFinished_returnsNull(){
        Dictionary dictionary = buildDictionary("word","translate","a small animal");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        trainer.handleAnswer("skip");

        assertNull(trainer.handleAnswer("not_skip"));
    }

    @Test
    void getProgressText_afterAnswer_remainingDecreases(){
        Dictionary dictionary = buildDictionary("word1","tr1","desc1","word2","tr2","desc2");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        assertTrue(trainer.getProgressText().contains("2 из 2"));
        trainer.handleAnswer("anything");
        assertTrue(trainer.getProgressText().contains("1 из 2"));
    }

    @Test
    void getResultText_afterCorrectAnswer_showsOne(){
        Dictionary dictionary = buildDictionary("word","translate","desc");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        trainer.handleAnswer("word");

        assertTrue(trainer.getResultText().contains("1 из 1"));
    }

    @Test
    void getResultText_afterWrongAnswer_showsZero(){
        Dictionary dictionary = buildDictionary("word","translate","desc");
        AbstractWordTrainer trainer = new DefinitionTrainer(dictionary);

        trainer.handleAnswer("wrong");

        assertTrue(trainer.getResultText().contains("0 из 1"));
    }

}
