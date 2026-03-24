package com.vladimir.wordtrainer.db;

import com.vladimir.wordtrainer.model.Word;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

public class DictionaryRepository {
    private final DataSource dataSource;

    public DictionaryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }

    private void init(){
        String sqlDictionary = "create table if not exists dictionary (\n" +
                "      id bigserial constraint pk_dictionary_id primary key,\n" +
                "      name varchar(255) not null,\n" +
                "      upload_telegram_user_id bigint constraint fk_dictionary_app_user_upload_telegram_user_id references app_user(telegram_user_id),\n" +
                "      is_public boolean not null default true,\n" +
                "      constraint unq_dictionary_name_upload_telegram_user_id unique(name, upload_telegram_user_id)\n" +
                "  );";

        String sqlWord = "create table if not exists word (\n" +
                "      id bigserial constraint pk_word_id primary key,\n" +
                "      dictionary_id bigint not null constraint fk_word_dictionary_dictionary_id references dictionary(id),\n" +
                "      english varchar(255) not null,\n" +
                "      russian varchar(255) not null,\n" +
                "      definition text not null,\n" +
                "      constraint unq_word_dictionary_id_english unique(dictionary_id, english)\n" +
                "  );";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatementDict = connection.prepareStatement(sqlDictionary);
            PreparedStatement preparedStatementWord = connection.prepareStatement(sqlWord)){

            preparedStatementDict.execute();
            preparedStatementWord.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания таблицы dictionary или word", e);
        }
    }

    public long saveDictionary(String name, long telegramUserId, boolean isPublic){
        String sql = "insert into dictionary (name, upload_telegram_user_id, is_public)\n" +
                "values (?,?,?)\n" +
                "on conflict (name, upload_telegram_user_id) do update set name = excluded.name\n" +
                "returning id";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, name);
            preparedStatement.setLong(2, telegramUserId);
            preparedStatement.setBoolean(3, isPublic);

            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) return rs.getLong(1);
            throw new RuntimeException("Не получили id словаря");

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения словаря.", e);
        }
    }

    public void saveWord(long dictionaryId, List<Word> wordList){
        String sql = "insert into word (dictionary_id, english, russian, definition)\n" +
                "values (?, ?, ?, ?)\n" +
                "on conflict (dictionary_id, english) do nothing";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){

            for(Word word : wordList) {
                preparedStatement.setLong(1, dictionaryId);
                preparedStatement.setString(2, word.getEnglish());
                preparedStatement.setString(3, word.getRussian());
                preparedStatement.setString(4, word.getEnglishDescription());
                preparedStatement.addBatch();
            }
            preparedStatement.executeBatch();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения слова.", e);
        }
    }

    public Map<Long, String> getUserSelectedDictionaries(long telegramUserId) {
        String sql = "select d.id, d.name\n" +
                     "  from user_dictionary ud\n" +
                     "  join dictionary d on d.id = ud.dictionary_id\n" +
                     " where ud.telegram_user_id = ?\n" +
                     " order by d.id";
        return queryDictionaries(sql, telegramUserId);
    }

    public Map<Long, String> getDictionariesToAdd(long telegramUserId) {
        String sql = "select d.id, d.name\n" +
                     "  from dictionary d\n" +
                     " where (d.upload_telegram_user_id = ? or d.is_public = true)\n" +
                     "   and not exists (\n" +
                     "       select 1 from user_dictionary ud\n" +
                     "        where ud.dictionary_id = d.id\n" +
                     "          and ud.telegram_user_id = ?\n" +
                     "   )\n" +
                     " order by d.id";
        return queryDictionaries2(sql, telegramUserId);
    }

    public Map<Long, String> getDictionariesToRemove(long telegramUserId) {
        String sql = "select d.id, d.name\n" +
                     "  from dictionary d\n" +
                     " where (d.upload_telegram_user_id = ? or d.is_public = true)\n" +
                     "   and exists (\n" +
                     "       select 1 from user_dictionary ud\n" +
                     "        where ud.dictionary_id = d.id\n" +
                     "          and ud.telegram_user_id = ?\n" +
                     "   )\n" +
                     " order by d.id";
        return queryDictionaries2(sql, telegramUserId);
    }

    public Map<Long, String> getOwnDictionaries(long telegramUserId) {
        String sql = "select d.id, d.name\n" +
                     "  from dictionary d\n" +
                     " where d.upload_telegram_user_id = ?\n" +
                     " order by d.id";
        return queryDictionaries(sql, telegramUserId);
    }

    private Map<Long, String> queryDictionaries(String sql, long telegramUserId) {
        Map<Long, String> dictionaries = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, telegramUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dictionaries.put(rs.getLong("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения словарей", e);
        }
        return dictionaries;
    }

    private Map<Long, String> queryDictionaries2(String sql, long telegramUserId) {
        Map<Long, String> dictionaries = new HashMap<>();
        try (Connection connection = dataSource.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setLong(1, telegramUserId);
            ps.setLong(2, telegramUserId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                dictionaries.put(rs.getLong("id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения словарей", e);
        }
        return dictionaries;
    }

    public void deleteDictionary(long dictionaryId) {
        String sqlDeleteUserDictionary = "delete from user_dictionary where dictionary_id = ?";
        String sqlDeleteWords = "delete from word where dictionary_id = ?";
        String sqlDeleteDictionary = "delete from dictionary where id = ?";

        try (Connection connection = dataSource.getConnection()) {
            connection.setAutoCommit(false);
            try (PreparedStatement psUserDict = connection.prepareStatement(sqlDeleteUserDictionary);
                 PreparedStatement psWords = connection.prepareStatement(sqlDeleteWords);
                 PreparedStatement psDict = connection.prepareStatement(sqlDeleteDictionary)) {

                psUserDict.setLong(1, dictionaryId);
                psWords.setLong(1, dictionaryId);
                psDict.setLong(1, dictionaryId);

                psUserDict.execute();
                psWords.execute();
                psDict.execute();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw new RuntimeException("Ошибка удаления словаря", e);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления словаря", e);
        }
    }

    public List<Word> getWordsByDictionaryId(long dictionaryId) {
        String sql = "select id, english, russian, definition from word where dictionary_id = ?";
        List<Word> words = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, dictionaryId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                words.add(new Word(rs.getLong("id"),
                                   rs.getString("english"),
                                   rs.getString("russian"),
                                   rs.getString("definition")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения слов словаря", e);
        }

        return words;
    }

    public byte[] getAudio(long wordId){
        String sql = "select audio_data from word where id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, wordId);
            ResultSet rs = preparedStatement.executeQuery();

            rs.next();
            return rs.getBytes("audio_data");
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при получении аудио для слова", e);
        }
    }

    public void saveAudio(long wordId, byte[] data){
        String sql = "update word set audio_data = ? where id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setBytes(1, data);
            preparedStatement.setLong(2, wordId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка при вставке аудио для слова", e);
        }
    }
}
