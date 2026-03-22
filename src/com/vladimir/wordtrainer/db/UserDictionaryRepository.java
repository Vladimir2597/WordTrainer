package com.vladimir.wordtrainer.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserDictionaryRepository {
    private final DataSource dataSource;

    public UserDictionaryRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }

    private void init() {
        String sql = "create table if not exists user_dictionary (\n" +
                "      telegram_user_id bigint not null constraint fk_user_dictionary_app_user_telegram_user_id references app_user(telegram_user_id),\n" +
                "      dictionary_id bigint not null constraint fk_user_dictionary_dictionary_dictionary_id references dictionary(id),\n" +
                "      constraint pk_user_dictionary primary key (telegram_user_id, dictionary_id)\n" +
                "  );";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания таблицы user_dictionary", e);
        }
    }

    public void addDictionaryToUser(long telegramUserId, long dictionaryId) {
        String sql = "insert into user_dictionary (telegram_user_id, dictionary_id)\n" +
                "values (?, ?)\n" +
                "on conflict do nothing";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, telegramUserId);
            preparedStatement.setLong(2, dictionaryId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления словаря пользователю", e);
        }
    }

    public void removeDictionaryFromUser(long telegramUserId, long dictionaryId) {
        String sql = "delete from user_dictionary where telegram_user_id = ? and dictionary_id = ?";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, telegramUserId);
            preparedStatement.setLong(2, dictionaryId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка удаления словаря пользователя", e);
        }
    }

    public void setAllPublicDictionariesToUser(long telegramUserId) {
        String sql = "insert into user_dictionary (telegram_user_id, dictionary_id)\n" +
                "select ?, d.id\n" +
                "  from dictionary d\n" +
                " where d.is_public = true\n" +
                "on conflict do nothing";

        try (Connection connection = dataSource.getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setLong(1, telegramUserId);
            preparedStatement.execute();
        } catch (SQLException e) {
            throw new RuntimeException("Ошибка добавления публичных словарей пользователю", e);
        }
    }

}