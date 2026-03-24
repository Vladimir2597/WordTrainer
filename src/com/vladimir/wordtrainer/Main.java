package com.vladimir.wordtrainer;

import com.vladimir.wordtrainer.bot.WordTrainerBot;
import com.vladimir.wordtrainer.db.DataSourceProvider;
import com.vladimir.wordtrainer.db.DictionaryRepository;
import com.vladimir.wordtrainer.db.UserDictionaryRepository;
import com.vladimir.wordtrainer.db.UserRepository;
import com.vladimir.wordtrainer.service.AudioService;
import com.vladimir.wordtrainer.service.DictionaryService;
import com.vladimir.wordtrainer.service.TrainingService;
import com.vladimir.wordtrainer.service.UserService;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import javax.sql.DataSource;

public class Main {
    public static void main(String[] args) {
        String botToken = System.getenv("BOT_TOKEN");
        String botUsername = System.getenv("BOT_USERNAME");
        String voiceRssKey = System.getenv("VOICERSS_KEY");

        if (botToken == null || botUsername == null || voiceRssKey == null ) {
            System.err.println("Ошибка: задайте переменные окружения BOT_TOKEN, BOT_USERNAME и VOICERSS_KEY");
            return;
        }

        DataSource dataSource = DataSourceProvider.create();
        UserRepository userRepository = new UserRepository(dataSource);
        DictionaryRepository dictionaryRepository = new DictionaryRepository(dataSource);
        UserDictionaryRepository userDictionaryRepository = new UserDictionaryRepository(dataSource);

        DictionaryService dictionaryService = new DictionaryService(dictionaryRepository, userDictionaryRepository);
        UserService userService = new UserService(userRepository, userDictionaryRepository);
        TrainingService trainingService = new TrainingService();
        AudioService audioService = new AudioService(voiceRssKey, dictionaryRepository);

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new WordTrainerBot(botToken,
                    botUsername,
                    dictionaryService,
                    userService,
                    trainingService,
                    audioService));
            System.out.println("Бот запущен!");
        } catch (TelegramApiException e) {
            System.err.println("Ошибка запуска бота: " + e.getMessage());
        }
    }
}