package com.vladimir.wordtrainer.bot.handler;

import com.vladimir.wordtrainer.bot.Callbacks;
import com.vladimir.wordtrainer.bot.MessageSender;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.service.DictionaryService;
import com.vladimir.wordtrainer.session.AppState;
import com.vladimir.wordtrainer.session.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.List;

public class DictionaryManagementHandler {
    private final MessageSender messageSender;
    private final DictionaryService dictionaryService;

    public DictionaryManagementHandler(MessageSender messageSender,
                                       DictionaryService dictionaryService) {
        this.messageSender = messageSender;
        this.dictionaryService = dictionaryService;
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
        boolean isPublic = data.equals(Callbacks.DICT_VISIBILITY_PUBLIC);
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
        InlineKeyboardButton publicBtn = new InlineKeyboardButton("🌍 Публичный");
        publicBtn.setCallbackData(Callbacks.DICT_VISIBILITY_PUBLIC);

        InlineKeyboardButton privateBtn = new InlineKeyboardButton("🔒 Только для меня");
        privateBtn.setCallbackData(Callbacks.DICT_VISIBILITY_PRIVATE);

        messageSender.sendWithKeyboard(chatId, "Словарь \"" + name + "\" готов к загрузке.\nКто может его видеть?",
                List.of(List.of(publicBtn), List.of(privateBtn)));
    }
}