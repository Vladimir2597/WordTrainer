package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.model.SentenceEvaluation;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

public class AIService {
    private final String apiKey;
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String URL_GROQ = "https://api.groq.com/openai/v1/chat/completions";
    private static final String MODEL = "llama-3.3-70b-versatile";
    private static final String PROMPT = "You are an English teacher. The student was given the word \"%s\" " +
            "and wrote this sentence: %s. " +
            "Evaluate if the word is used correctly and if the sentence is grammatically acceptable. " +
            "Respond ONLY in JSON format: {\"percentage\": 0-100, \"feedback\": \"пояснение на русском\"}";

    public AIService(String apiKey) {
        this.apiKey = apiKey;
    }

    public SentenceEvaluation evaluate(String word, String sentence) {
        HttpClient client = HttpClient.newHttpClient();
        String promptToSend = PROMPT.formatted(word, sentence);

        ObjectNode messageNode = mapper.createObjectNode()
                .put("role", "user")
                .put("content", promptToSend);
        ArrayNode messagesArray = mapper.createArrayNode().add(messageNode);
        String body = mapper.createObjectNode()
                .put("model", MODEL)
                .set("messages", messagesArray).toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(URL_GROQ))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());

            JsonNode choices = root.path("choices");
            if (choices.isEmpty()) {
                System.err.println("Groq error: " + response.body());
                return null;
            }

            String text = choices.get(0)
                    .path("message")
                    .path("content")
                    .asText();

            text = text.replaceAll("```json\\s*|```\\s*", "").trim();

            JsonNode result = mapper.readTree(text);
            int percentage = result.path("percentage").asInt();
            String feedback = result.path("feedback").asText();

            return new SentenceEvaluation(percentage, feedback);
        } catch (IOException e) {
            System.err.println("Ошибка во время запроса к Groq: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}