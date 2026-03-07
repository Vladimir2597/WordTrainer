package com.vladimir.wordtrainer.service;

public interface Trainer {
    String getNextQuestion();
    String handleAnswer(String answer);
    boolean isFinished();
}