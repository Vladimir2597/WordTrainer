package com.vladimir.wordtrainer.model;

public class SentenceEvaluation {
    private final int percentage;
    private final String feedback;

    public SentenceEvaluation(int percentage, String feedback) {
        this.percentage = percentage;
        this.feedback = feedback;
    }

    public int getPercentage() { return percentage; }
    public String getFeedback() { return feedback; }
}
