package com.vladimir.wordtrainer;

import com.vladimir.wordtrainer.bot.WordTrainerBot;
import com.vladimir.wordtrainer.service.AudioService;
import com.vladimir.wordtrainer.service.DictionaryManager;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class Main {
    public static void main(String[] args) {
        String botToken = System.getenv("BOT_TOKEN");
        String botUsername = System.getenv("BOT_USERNAME");
        String voiceRssKey = System.getenv("VOICERSS_KEY");

        if (botToken == null || botUsername == null || voiceRssKey == null ) {
            System.err.println("Ошибка: задайте переменные окружения BOT_TOKEN, BOT_USERNAME и VOICERSS_KEY");
            return;
        }

        DictionaryManager dictionaryManager = new DictionaryManager("dictionaries.txt");
        AudioService audioService = new AudioService(voiceRssKey, "audio");

        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new WordTrainerBot(botToken, botUsername, dictionaryManager, audioService));
            System.out.println("Бот запущен!");
        } catch (TelegramApiException e) {
            System.err.println("Ошибка запуска бота: " + e.getMessage());
        }
    }
}