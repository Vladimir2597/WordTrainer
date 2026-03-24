package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.db.DictionaryRepository;
import com.vladimir.wordtrainer.model.Word;
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
    private final DictionaryRepository dictionaryRepository;
    private final static String AUDIO_DIR = "tempAudio" ;

    public AudioService(String apiKey, DictionaryRepository dictionaryRepository){
        this.apiKey = apiKey;
        this.dictionaryRepository = dictionaryRepository;
    }

    public File getAudio(Word word) {
        byte[] audioBytes = dictionaryRepository.getAudio(word.getId());
        if (audioBytes != null) {
            try {
                File tempFile = File.createTempFile("audio_", ".ogg");
                tempFile.deleteOnExit();
                Files.write(tempFile.toPath(), audioBytes);
                return tempFile;
            } catch (IOException e) {
                System.err.println("Не удалось создать temp файл: " + e.getMessage());
            }
        }

        HttpClient client = HttpClient.newHttpClient();
        String encodedWord = URLEncoder.encode(word.getEnglish(), StandardCharsets.UTF_8);
        String url = "http://api.voicerss.org/?key=" + apiKey
                + "&hl=en-gb&src=" + encodedWord
                + "&c=MP3&f=16khz_16bit_mono";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .GET()
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());

            File mp3Temp = File.createTempFile("audio_", ".mp3");
            File oggTemp = File.createTempFile("audio_", ".ogg");
            mp3Temp.deleteOnExit();
            oggTemp.deleteOnExit();

            String contentType = response.headers().firstValue("Content-Type").orElse("");
            if (!contentType.contains("audio")) {
                System.err.println("VoiceRSS error: " + new String(response.body()));
                return null;
            }
            
            Files.write(mp3Temp.toPath(), response.body());

            boolean converted = convertToOpusOgg(mp3Temp, oggTemp);
            dictionaryRepository.saveAudio(word.getId(), Files.readAllBytes(oggTemp.toPath()));

            return converted ? oggTemp : null;
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
