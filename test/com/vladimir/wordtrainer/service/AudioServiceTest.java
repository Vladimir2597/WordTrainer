package com.vladimir.wordtrainer.service;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class AudioServiceTest {
    @TempDir
    Path tempDir;
    private static final String voiceRssKey = System.getenv("VOICERSS_KEY");

    @Test
    void getAudio_shouldReturnExistingOggFile() throws IOException {
        String word = "Hello World".toLowerCase().replace(" ", "_");
        File existingOggFile = tempDir.resolve(word + ".ogg").toFile();
        Files.write(existingOggFile.toPath(), new byte[] {1, 2, 3});

        AudioService audioService = new AudioService(
                voiceRssKey,
                tempDir.toString()
        );

        File result = audioService.getAudio(word);

        assertNotNull(result);
        assertArrayEquals(Files.readAllBytes(existingOggFile.toPath()),
                Files.readAllBytes(result.toPath()));
        assertTrue(result.exists());
    }

    @Test
    void getAudio_shouldReturnNewOggFile() throws IOException {
        String word = "Hello World".toLowerCase().replace(" ", "_");
        File file = tempDir.resolve(word + ".ogg").toFile();

        assertFalse(file.exists());

        AudioService audioService = new AudioService(
                voiceRssKey,
                tempDir.toString()
        );

        File result = audioService.getAudio(word);

        assertNotNull(result);
        assertTrue(result.exists());
    }
}
