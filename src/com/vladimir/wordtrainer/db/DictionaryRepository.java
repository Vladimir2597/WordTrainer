package com.vladimir.wordtrainer.db;

import com.vladimir.wordtrainer.model.Word;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DictionaryRepository {
    private final DataSource dataSource;

    public DictionaryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }

    private void init(){
        String sqlDictionary = "create table if not exists dictionary (\n" +
                "      id bigserial constraint pk_dictionary_id primary key,\n" +
                "      name varchar(255) not null constraint unq_dictionary_name unique,\n" +
                "      telegram_user_id bigint constraint fk_dictionary_app_user_telegram_user_id references app_user(telegram_user_id),\n" +
                "      is_public boolean not null default true\n" +
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

    public long saveDictionary(String name){
        String sql = "insert into dictionary (name)\n" +
                "values (?)\n" +
                "on conflict (name) do update set name = excluded.name\n" +
                "returning id";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, name);

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

    public Map<Long, String> getAllDictionaries(){
        String sql = "select id, name from dictionary";
        Map<Long, String> dictionaries = new HashMap<>();

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            ResultSet resultSet = preparedStatement.executeQuery();

            while (resultSet.next()) {
                dictionaries.put(resultSet.getLong("id"),
                        resultSet.getString("name"));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка во время получения всех словарей", e);
        }

        return dictionaries;
    }

    public List<Word> getWordsByDictionaryId(long dictionaryId) {
        String sql = "select english, russian, definition from word where dictionary_id = ?";
        List<Word> words = new ArrayList<>();

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, dictionaryId);
            ResultSet rs = preparedStatement.executeQuery();

            while (rs.next()) {
                words.add(new Word(rs.getString("english"), rs.getString("russian"), rs.getString("definition")));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка получения слов словаря", e);
        }

        return words;
    }
}
