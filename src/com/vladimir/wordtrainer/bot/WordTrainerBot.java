package com.vladimir.wordtrainer.bot;

import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.service.AbstractWordTrainer;
import com.vladimir.wordtrainer.service.DefinitionTrainer;
import com.vladimir.wordtrainer.service.DictionaryManager;
import com.vladimir.wordtrainer.service.RusToEngTrainer;
import com.vladimir.wordtrainer.session.AppState;
import com.vladimir.wordtrainer.session.UserSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordTrainerBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final DictionaryManager dictionaryManager;
    private final Map<Long, UserSession> sessions = new HashMap<>();

    public WordTrainerBot(String botToken, String botUsername, DictionaryManager dictionaryManager) {
        super(botToken);
        this.botUsername = botUsername;
        this.dictionaryManager = dictionaryManager;
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText().trim();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            handleMessage(chatId, text, session);
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            handleCallback(chatId, data, session);
        }
    }

    private void handleMessage(long chatId, String text, UserSession session) {
        if (text.equals("/start")) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            sendDictionaryList(chatId);
            return;
        }

        switch (session.getState()) {
            case CHOOSING_DICTIONARY -> sendDictionaryList(chatId);
            case CHOOSING_MODE -> sendModeSelection(chatId, session.getDictionary().getName());
            case TRAINING -> handleTrainingAnswer(chatId, text, session);
        }
    }

    private void handleCallback(long chatId, String data, UserSession session) {
        if (data.startsWith("dict:")) {
            int index = Integer.parseInt(data.substring(5));
            Dictionary dictionary = dictionaryManager.loadDictionaryByIndex(index);
            session.setDictionary(dictionary);
            session.setState(AppState.CHOOSING_MODE);
            sendModeSelection(chatId, dictionary.getName());

        } else if (data.startsWith("mode:")) {
            String mode = data.substring(5);
            AbstractWordTrainer trainer = switch (mode) {
                case "definition" -> new DefinitionTrainer(session.getDictionary());
                case "russian" -> new RusToEngTrainer(session.getDictionary());
                default -> null;
            };
            if (trainer == null) return;

            session.setTrainer(trainer);
            session.setState(AppState.TRAINING);
            sendNextQuestion(chatId, session);

        } else if (data.equals("retry_wrong")) {
            session.getTrainer().resetWithWrongOnly();
            sendNextQuestion(chatId, session);

        } else if (data.equals("retry_all")) {
            session.getTrainer().resetAll();
            sendNextQuestion(chatId, session);

        } else if (data.equals("back_to_menu")) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            sendDictionaryList(chatId);
        }
    }

    private void handleTrainingAnswer(long chatId, String text, UserSession session) {
        if (text.equalsIgnoreCase("/menu")) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            sendDictionaryList(chatId);
            return;
        }

        AbstractWordTrainer trainer = session.getTrainer();
        String result = trainer.handleAnswer(text);

        if (trainer.isFinished()) {
            sendFinishMenu(session, chatId, result + "\n\n" + trainer.getResultText());
        } else {
            sendText(chatId, result);
            sendNextQuestion(chatId, session);
        }
    }

    private void sendNextQuestion(long chatId, UserSession session) {
        String question = session.getTrainer().getNextQuestion();
        if (question == null) {
            sendFinishMenu(session, chatId, session.getTrainer().getResultText());
            return;
        }
        sendText(chatId, session.getTrainer().getProgressText() + "\n\n" + question);
    }

    private void sendDictionaryList(long chatId) {
        List<String> names = dictionaryManager.getNames();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            InlineKeyboardButton button = new InlineKeyboardButton(names.get(i));
            button.setCallbackData("dict:" + i);
            rows.add(List.of(button));
        }
        sendWithKeyboard(chatId, "📚 Выберите словарь:", rows);
    }

    private void sendModeSelection(long chatId, String dictionaryName) {
        InlineKeyboardButton byDefinition = new InlineKeyboardButton("По определению на английском");
        byDefinition.setCallbackData("mode:definition");

        InlineKeyboardButton byRussian = new InlineKeyboardButton("По слову на русском");
        byRussian.setCallbackData("mode:russian");

        sendWithKeyboard(chatId, "Вы выбрали: " + dictionaryName + "\n\nВыберите режим:",
                List.of(List.of(byDefinition), List.of(byRussian)));
    }

    private void sendFinishMenu(UserSession session, long chatId, String text) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (session.getTrainer().existsMoreWords()) {
            InlineKeyboardButton retryWrong = new InlineKeyboardButton("Повторить неправильные");
            retryWrong.setCallbackData("retry_wrong");
            keyboard.add(List.of(retryWrong));
        }
        InlineKeyboardButton retryAll = new InlineKeyboardButton("Повторить всё заново");
        retryAll.setCallbackData("retry_all");
        keyboard.add(List.of(retryAll));

        InlineKeyboardButton backToMenu = new InlineKeyboardButton("Выбрать другой словарь");
        backToMenu.setCallbackData("back_to_menu");
        keyboard.add(List.of(backToMenu));

        sendWithKeyboard(chatId, "♻️ " + text + "\n\nЧто делаем дальше?", keyboard);
    }

    private void sendText(long chatId, String text) {
        try {
            execute(new SendMessage(String.valueOf(chatId), text));
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }

    private void sendWithKeyboard(long chatId, String text, List<List<InlineKeyboardButton>> rows) {
        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        markup.setKeyboard(rows);

        SendMessage message = new SendMessage(String.valueOf(chatId), text);
        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }
}
