package com.vladimir.wordtrainer.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class UserRepository {
    private final DataSource dataSource;

    public UserRepository(DataSource dataSource) {
        this.dataSource = dataSource;
        init();
    }

    private void init(){
        String sql = "create table if not exists app_user (\n" +
                "    id bigserial constraint pk_app_user_id primary key,\n" +
                "    telegram_user_id bigint not null constraint unq_app_user_telegram_user_id unique,\n" +
                "    username varchar(255),\n" +
                "    first_name varchar(255),\n" +
                "    last_name varchar(255),\n" +
                "    created_at timestamp not null default current_timestamp\n" +
                ");";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка создания таблицы app_user", e);
        }
    }

    public void saveUserIfNotExists(Long telegramUserId, String username, String firstName, String lastName){
        String sql = "insert into app_user (telegram_user_id, username, first_name, last_name)\n" +
                "values (?,?,?,?)\n" +
                "on conflict (telegram_user_id) do nothing";

        try(Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setLong(1, telegramUserId);
            preparedStatement.setString(2, username);
            preparedStatement.setString(3,firstName);
            preparedStatement.setString(4,lastName);

            preparedStatement.execute();

        } catch (SQLException e) {
            throw new RuntimeException("Ошибка сохранения пользователя", e);
        }
    }
}
