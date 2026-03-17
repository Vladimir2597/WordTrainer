package com.vladimir.wordtrainer.service;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class AudioService {
    private final String apiKey;
    private final String audioDir ;

    public AudioService(String apiKey, String audioDir){
        this.apiKey = apiKey;
        this.audioDir = audioDir;
    }

    public File getAudio(String word) {
        File dir = new File(audioDir);
        dir.mkdirs();

        String wordFileName = word.replace(" ", "_").toLowerCase() + ".mp3";
        File file = new File(dir, wordFileName);

        if (file.exists()) {
            return file;
        }

        HttpClient client = HttpClient.newHttpClient();

        String encodedWord = URLEncoder.encode(word, StandardCharsets.UTF_8);
        String url = "http://api.voicerss.org/?key=" + apiKey
                + "&hl=en-gb&src=" + encodedWord
                + "&c=MP3&f=16khz_16bit_mono";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!contentType.contains("audio")) {
                System.err.println("VoiceRSS error: " + new String(response.body()));
                return null;
            }

            Files.write(file.toPath(), response.body());
            return file;

        } catch (IOException e) {
            System.err.println("Ошибка запроса к VoiceRSS: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }
}
