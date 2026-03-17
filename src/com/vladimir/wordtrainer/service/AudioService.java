package com.vladimir.wordtrainer.service;

import ws.schild.jave.Encoder;
import ws.schild.jave.MultimediaObject;
import ws.schild.jave.encode.AudioAttributes;
import ws.schild.jave.encode.EncodingAttributes;

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

        String baseName = word.replace(" ", "_").toLowerCase();
        File oggFile = new File(dir, baseName + ".ogg");

        if (oggFile.exists()) {
            return oggFile;
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

            File mp3File = new File(dir, baseName + ".mp3");
            Files.write(mp3File.toPath(), response.body());

            boolean converted = convertToOpusOgg(mp3File, oggFile);
            mp3File.delete();

            return converted ? oggFile : null;

        } catch (IOException e) {
            System.err.println("Ошибка запроса к VoiceRSS: " + e.getMessage());
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private boolean convertToOpusOgg(File input, File output) {
        try {
            AudioAttributes audio = new AudioAttributes();
            audio.setCodec("libopus");
            audio.setBitRate(64000);
            audio.setSamplingRate(16000);
            audio.setChannels(1);

            EncodingAttributes attrs = new EncodingAttributes();
            attrs.setOutputFormat("ogg");
            attrs.setAudioAttributes(audio);

            Encoder encoder = new Encoder();
            encoder.encode(new MultimediaObject(input), output, attrs);
            return true;
        } catch (Exception e) {
            System.err.println("Ошибка конвертации: " + e.getMessage());
            return false;
        }
    }
}
