package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.service.trainer.DefinitionTrainer;
import com.vladimir.wordtrainer.service.trainer.RusToEngTrainer;
import com.vladimir.wordtrainer.service.trainer.Trainer;

public class TrainingService {
    public Trainer createTrainer(String mode, Dictionary dictionary) {
        return switch (mode) {
            case "definition" -> new DefinitionTrainer(dictionary);
            case "russian" -> new RusToEngTrainer(dictionary);
            default -> throw new IllegalArgumentException("Неизвестный режим: " + mode);
        };
    }
}
