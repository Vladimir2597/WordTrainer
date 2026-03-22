package com.vladimir.wordtrainer.bot.handler;

import com.vladimir.wordtrainer.bot.Callbacks;
import com.vladimir.wordtrainer.bot.MessageSender;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.service.DictionaryService;
import com.vladimir.wordtrainer.session.AppState;
import com.vladimir.wordtrainer.session.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DictionaryManagementHandler {
    private final MessageSender messageSender;
    private final DictionaryService dictionaryService;

    public DictionaryManagementHandler(MessageSender messageSender,
                                       DictionaryService dictionaryService) {
        this.messageSender = messageSender;
        this.dictionaryService = dictionaryService;
    }

    public void sendManagementMenu(long chatId) {
        InlineKeyboardButton addExisting = new InlineKeyboardButton(Callbacks.ADD_EXISTING_DICTIONARY.buttonText());
        addExisting.setCallbackData(Callbacks.ADD_EXISTING_DICTIONARY.callback());

        InlineKeyboardButton removeDict = new InlineKeyboardButton(Callbacks.REMOVE_DICTIONARY.buttonText());
        removeDict.setCallbackData(Callbacks.REMOVE_DICTIONARY.callback());

        InlineKeyboardButton uploadBtn = new InlineKeyboardButton(Callbacks.UPLOAD_DICTIONARY.buttonText());
        uploadBtn.setCallbackData(Callbacks.UPLOAD_DICTIONARY.callback());

        InlineKeyboardButton deleteBtn = new InlineKeyboardButton(Callbacks.DELETE_DICTIONARY.buttonText());
        deleteBtn.setCallbackData(Callbacks.DELETE_DICTIONARY.callback());

        messageSender.sendWithKeyboard(chatId, "⚙️ Управление словарями:",
                List.of(List.of(addExisting), List.of(removeDict), List.of(uploadBtn), List.of(deleteBtn)));
    }

    public void handleFileUploaded(long chatId, File file, UserSession session) {
        String name = dictionaryService.parseDictionaryName(file);
        List<Word> words = dictionaryService.parseDictionaryWords(file);
        session.setPendingDictionaryName(name);
        session.setPendingWords(words);
        sendAskDictionaryVisibility(chatId, name);
    }

    public void handleUploadRequest(long chatId, UserSession session) {
        messageSender.sendText(chatId, "Пришлите .txt файл в формате: первая строка — название словаря, далее\n" +
                "  слова в формате english = russian = definition");
        session.setState(AppState.UPLOADING_DICTIONARY);
    }

    public void handleVisibilityCallback(long chatId, String data, UserSession session) {
        boolean isPublic = data.equals(Callbacks.DICT_VISIBILITY_PUBLIC.callback());
        String name = session.getPendingDictionaryName();
        List<Word> words = session.getPendingWords();

        long dictionaryId = dictionaryService.saveDictionary(name, session.getTelegramUserId(), words, isPublic);
        dictionaryService.addDictionaryToUser(session.getTelegramUserId(), dictionaryId);

        session.setPendingDictionaryName(null);
        session.setPendingWords(null);
        session.setState(AppState.CHOOSING_DICTIONARY);

        messageSender.sendText(chatId, "Словарь \"" + name + "\" загружен! " + words.size() + " слов.");
    }

    public void sendAskDictionaryVisibility(long chatId, String name) {
        InlineKeyboardButton publicBtn = new InlineKeyboardButton(Callbacks.DICT_VISIBILITY_PUBLIC.buttonText());
        publicBtn.setCallbackData(Callbacks.DICT_VISIBILITY_PUBLIC.callback());

        InlineKeyboardButton privateBtn = new InlineKeyboardButton(Callbacks.DICT_VISIBILITY_PRIVATE.buttonText());
        privateBtn.setCallbackData(Callbacks.DICT_VISIBILITY_PRIVATE.callback());

        messageSender.sendWithKeyboard(chatId, "Словарь \"" + name + "\" готов к загрузке.\nКто может его видеть?",
                List.of(List.of(publicBtn), List.of(privateBtn)));
    }

    public void sendAddDictionaryList(long chatId, UserSession session) {
        Map<Long, String> dictionaries = dictionaryService.getDictionariesToAdd(session.getTelegramUserId());
        if (dictionaries.isEmpty()) {
            messageSender.sendText(chatId, "Нет доступных словарей для добавления.");
            return;
        }
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Long dictionaryId : dictionaries.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(dictionaries.get(dictionaryId));
            button.setCallbackData(Callbacks.ADD_DICT_PREFIX.callback() + dictionaryId);
            rows.add(List.of(button));
        }
        messageSender.sendWithKeyboard(chatId, "Выберите словарь для добавления:", rows);
    }

    public void handleAddDictionary(long chatId, long dictionaryId, UserSession session) {
        dictionaryService.addDictionaryToUser(session.getTelegramUserId(), dictionaryId);
        messageSender.sendText(chatId, "Словарь добавлен в ваш список.");
    }

    public void sendRemoveDictionaryList(long chatId, UserSession session) {
        Map<Long, String> dictionaries = dictionaryService.getDictionariesToRemove(session.getTelegramUserId());
        if (dictionaries.isEmpty()) {
            messageSender.sendText(chatId, "Ваш список словарей пуст.");
            return;
        }
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Long dictionaryId : dictionaries.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(dictionaries.get(dictionaryId));
            button.setCallbackData(Callbacks.REMOVE_DICT_PREFIX.callback() + dictionaryId);
            rows.add(List.of(button));
        }
        messageSender.sendWithKeyboard(chatId, "Выберите словарь для удаления из вашего списка:", rows);
    }

    public void sendDeleteDictionaryList(long chatId, UserSession session) {
        Map<Long, String> dictionaries = dictionaryService.getOwnDictionaries(session.getTelegramUserId());
        if (dictionaries.isEmpty()) {
            messageSender.sendText(chatId, "У вас нет собственных словарей для удаления.");
            return;
        }
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (Long dictionaryId : dictionaries.keySet()) {
            InlineKeyboardButton button = new InlineKeyboardButton(dictionaries.get(dictionaryId));
            button.setCallbackData(Callbacks.DELETE_DICT_PREFIX.callback() + dictionaryId);
            rows.add(List.of(button));
        }
        messageSender.sendWithKeyboard(chatId, "Выберите словарь для полного удаления:", rows);
    }

    public void handleRemoveDictionary(long chatId, long dictionaryId, UserSession session) {
        dictionaryService.removeDictionaryFromUser(session.getTelegramUserId(), dictionaryId);
        messageSender.sendText(chatId, "Словарь убран из вашего списка.");
    }

public void handleDeleteDictionary(long chatId, long dictionaryId) {
        dictionaryService.deleteDictionary(dictionaryId);
        messageSender.sendText(chatId, "Словарь удалён полностью.");
    }
}