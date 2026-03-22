package com.vladimir.wordtrainer.bot.handler;

import com.vladimir.wordtrainer.bot.Callbacks;
import com.vladimir.wordtrainer.bot.MessageSender;
import com.vladimir.wordtrainer.service.DictionaryService;
import com.vladimir.wordtrainer.session.AppState;
import com.vladimir.wordtrainer.session.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PreTrainingHandler {
    private final MessageSender messageSender;
    private final DictionaryService dictionaryService;

    public PreTrainingHandler(MessageSender messageSender, DictionaryService dictionaryService) {
        this.messageSender = messageSender;
        this.dictionaryService = dictionaryService;
    }

    public DictionaryService getDictionaryService() {
        return dictionaryService;
    }

    public void handleDictionaryCommand(long chatId, UserSession session) {
        if (session.getState() == AppState.TRAINING) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            session.setTrainer(null);
        }
        sendDictionaryList(chatId, session);
    }

    public void handleModeCommand(long chatId, UserSession session) {
        if (session.getState() == AppState.TRAINING) {
            session.setState(AppState.CHOOSING_MODE);
            session.setTrainer(null);
        }

        if (session.getDictionary() == null) {
            messageSender.sendText(chatId, "Сначала выберите словарь.");
            sendDictionaryList(chatId, session);
        } else {
            sendModeSelection(chatId, session.getDictionary().getName());
        }
    }

    public void sendDictionaryList(long chatId, UserSession session) {
        session.setAvailableDictionary(
                dictionaryService.getUserSelectedDictionaries(session.getTelegramUserId())
        );
        Map<Long, String> dictionaries = session.getAvailableDictionary();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        for (Long dictionaryId : dictionaries.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(dictionaries.get(dictionaryId));
            button.setCallbackData(Callbacks.DICT_PREFIX.callback() + dictionaryId);
            rows.add(List.of(button));
        }

        messageSender.sendWithKeyboard(chatId, "📚 Выберите словарь:", rows);
    }

    public void sendModeSelection(long chatId, String dictionaryName) {
        InlineKeyboardButton byDefinition = new InlineKeyboardButton(Callbacks.MODE_DEFINITION.buttonText());
        byDefinition.setCallbackData(Callbacks.MODE_DEFINITION.callback());

        InlineKeyboardButton byRussian = new InlineKeyboardButton(Callbacks.MODE_RUSSIAN.buttonText());
        byRussian.setCallbackData(Callbacks.MODE_RUSSIAN.callback());

        InlineKeyboardButton backToMenu = new InlineKeyboardButton(Callbacks.BACK_TO_MENU.buttonText());
        backToMenu.setCallbackData(Callbacks.BACK_TO_MENU.callback());

        messageSender.sendWithKeyboard(chatId, "Вы выбрали: " + dictionaryName + "\n\nВыберите режим:",
                List.of(List.of(byDefinition), List.of(byRussian), List.of(backToMenu)));
    }
}