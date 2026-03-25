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
    private static final String URL_GEMINI = "https://generativelanguage.googleapis.com/" +
            "v1beta/models/gemini-3-flash-preview:generateContent?key=";
    private static final String PROMPT = "You are an English teacher. The student was given the word \"%s\" " +
            "and wrote this sentence: %s. " +
            "Evaluate if the word is used correctly and if the sentence is grammatically acceptable. " +
            "Respond ONLY in JSON format: {\"percentage\": 0-100, \"feedback\": \"пояснение на русском\"}";

    public AIService(String apiKey) {
        this.apiKey = apiKey;
    }

    public SentenceEvaluation evaluate(String word, String sentence) {
        HttpClient client = HttpClient.newHttpClient();
        String url = URL_GEMINI + apiKey;
        String promptToSend = PROMPT.formatted(word, sentence);

        ObjectNode partNode = mapper.createObjectNode().put("text", promptToSend);
        ArrayNode partsArray = mapper.createArrayNode().add(partNode);
        ObjectNode contentNode = mapper.createObjectNode().set("parts", partsArray);
        ArrayNode contentsArray = mapper.createArrayNode().add(contentNode);
        String body = mapper.createObjectNode().set("contents", contentsArray).toString();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            JsonNode root = mapper.readTree(response.body());

            JsonNode candidates = root.path("candidates");
            if (candidates.isEmpty()) {
                System.err.println("Gemini error: " + response.body());
                return null;
            }

            String text = candidates.get(0)
                    .path("content")
                    .path("parts")
                    .get(0)
                    .path("text")
                    .asText();

            text = text.replaceAll("```json\\s*|```\\s*", "").trim();

            JsonNode result = mapper.readTree(text);
            int percentage = result.path("percentage").asInt();
            String feedback = result.path("feedback").asText();

            return new SentenceEvaluation(percentage, feedback);
        } catch (IOException e) {
            System.err.println("Ошибка во время запроса к Gemini: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}