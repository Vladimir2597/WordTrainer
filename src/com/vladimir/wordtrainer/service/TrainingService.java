package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.service.trainer.DefinitionTrainer;
import com.vladimir.wordtrainer.service.trainer.RusToEngTrainer;
import com.vladimir.wordtrainer.service.trainer.Trainer;

public class TrainingService {
    public static final String MODE_DEFINITION = "definition";
    public static final String MODE_RUSSIAN = "russian";

    public Trainer createTrainer(String mode, Dictionary dictionary) {
        return switch (mode) {
            case MODE_DEFINITION -> new DefinitionTrainer(dictionary);
            case MODE_RUSSIAN -> new RusToEngTrainer(dictionary);
            default -> throw new IllegalArgumentException("Неизвестный режим: " + mode);
        };
    }
}
