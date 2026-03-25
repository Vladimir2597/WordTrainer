package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.service.trainer.DefinitionTrainer;
import com.vladimir.wordtrainer.service.trainer.RusToEngTrainer;
import com.vladimir.wordtrainer.service.trainer.SentenceTrainer;
import com.vladimir.wordtrainer.service.trainer.Trainer;

public class TrainingService {
    public static final String MODE_DEFINITION = "definition";
    public static final String MODE_RUSSIAN = "russian";
    public static final String MODE_SENTENCE = "sentence";
    private final AIService aiService;

    public TrainingService(AIService aiService) {
        this.aiService = aiService;
    }

    public Trainer createTrainer(String mode, Dictionary dictionary) {
        return switch (mode) {
            case MODE_DEFINITION -> new DefinitionTrainer(dictionary);
            case MODE_RUSSIAN -> new RusToEngTrainer(dictionary);
            case MODE_SENTENCE -> new SentenceTrainer(dictionary, aiService);
            default -> throw new IllegalArgumentException("Неизвестный режим: " + mode);
        };
    }
}
