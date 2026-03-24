package com.vladimir.wordtrainer.service;

import com.vladimir.wordtrainer.db.DataSourceProvider;
import com.vladimir.wordtrainer.db.DictionaryRepository;
import com.vladimir.wordtrainer.model.Word;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

@Tag("local-only")
public class AudioServiceTest {
    private static final DataSource dataSource = DataSourceProvider.create();
    private static final DictionaryRepository dictionaryRepository = new DictionaryRepository(dataSource);
    private Word word;

    private void clearAudio(long wordId) {
        String sql = "update word set audio_data = null where id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, wordId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при очистке аудио", e);
        }
    }

    @BeforeEach
    void getRandomWord() {
        String sql = "select w.id, w.english, w.russian, w.definition " +
                "  from word w " +
                " limit 1";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            ResultSet rs = preparedStatement.executeQuery();
            if (!rs.next()) throw new RuntimeException("Нет слов с аудио в базе");
            word = new Word(rs.getLong("id"),
                    rs.getString("english"),
                    rs.getString("russian"),
                    rs.getString("definition"));
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении слова с аудио", e);
        }
    }

    @Test
    void getAudio_shouldReturnFileFromDb() {
        String voiceRssKey = System.getenv("VOICERSS_KEY");
        assertNotNull(voiceRssKey, "VOICERSS_KEY не задан — невозможно подготовить аудио в базе");
        AudioService audioService = new AudioService(voiceRssKey, dictionaryRepository);
        audioService.getAudio(word); // чтобы аудио точно существовало -> будет браться файл с БД
        audioService = new AudioService("incorrect_voiceRssKey", dictionaryRepository);

        File result = audioService.getAudio(word);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.getName().endsWith(".ogg"));
    }

    @Test
    void getAudio_shouldDownloadAndSaveToDb() {
        String voiceRssKey = System.getenv("VOICERSS_KEY");

        clearAudio(word.getId()); // чтобы аудио точно не существовало -> будет вызываться API
        AudioService audioService = new AudioService(voiceRssKey, dictionaryRepository);

        File result = audioService.getAudio(word);

        assertNotNull(result);
        assertTrue(result.exists());
        assertTrue(result.getName().endsWith(".ogg"));
        assertNotNull(dictionaryRepository.getAudio(word.getId()));
    }

    @Test
    void getAudio_shouldReturnNullWhenNoAudioInDbAndInvalidKey() {
        clearAudio(word.getId());
        AudioService audioService = new AudioService("incorrect_voiceRssKey", dictionaryRepository);

        File result = audioService.getAudio(word);

        assertNull(result);
    }
}
