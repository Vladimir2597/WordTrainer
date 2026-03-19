package com.vladimir.wordtrainer.bot;

import com.vladimir.wordtrainer.db.DictionaryRepository;
import com.vladimir.wordtrainer.db.UserRepository;
import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.service.*;
import com.vladimir.wordtrainer.session.AppState;
import com.vladimir.wordtrainer.session.UserSession;
import com.vladimir.wordtrainer.util.FileUtil;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordTrainerBot extends TelegramLongPollingBot {
    private final String botUsername;
    private final DictionaryManager dictionaryManager;
    private final AudioService audioService;
    private final UserRepository userRepository;
    private final DictionaryRepository dictionaryRepository;
    private final Map<Long, UserSession> sessions = new HashMap<>();

    public WordTrainerBot(String botToken,
                          String botUsername,
                          DictionaryManager dictionaryManager,
                          AudioService audioService,
                          UserRepository userRepository,
                          DictionaryRepository dictionaryRepository) {
        super(botToken);
        this.botUsername = botUsername;
        this.dictionaryManager = dictionaryManager;
        this.audioService = audioService;
        this.userRepository = userRepository;
        this.dictionaryRepository = dictionaryRepository;
        registerBotCommands();
    }

    @Override
    public String getBotUsername() {
        return botUsername;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            org.telegram.telegrambots.meta.api.objects.User from = update.getMessage().getFrom();
            userRepository.saveUserIfNotExists(from.getId(), from.getUserName(), from.getFirstName(), from.getLastName());
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText().trim();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            handleMessage(chatId, text, session);
        } else if (update.hasCallbackQuery()) {
            org.telegram.telegrambots.meta.api.objects.User from = update.getCallbackQuery().getFrom();
            userRepository.saveUserIfNotExists(from.getId(), from.getUserName(), from.getFirstName(), from.getLastName());
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            handleCallback(chatId, data, session);
        } else if (update.getMessage().hasDocument()){
            long chatId = update.getMessage().getChatId();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            if (session.getState() == AppState.UPLOADING_DICTIONARY) {
                String fileId = update.getMessage().getDocument().getFileId();
                try {
                    File file = downloadFile(execute(new GetFile(fileId)));

                    String name = FileUtil.loadDictionaryName(file);
                    List<Word> words = FileUtil.loadWordsFromFile(file);

                    long dictionaryId = dictionaryRepository.saveDictionary(name);
                    dictionaryRepository.saveWord(dictionaryId, words);

                    sendText(chatId, "Словарь \"" + name + "\" загружен! " + words.size() + " слов.");
                    session.setState(AppState.CHOOSING_DICTIONARY);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void registerBotCommands() {
        SetMyCommands setMyCommands = new SetMyCommands();
        List<BotCommand> commands = List.of(
                new BotCommand("/dictionary", "Выбрать словарь"),
                new BotCommand("/mode", "Сменить режим обучения")
        );
        setMyCommands.setCommands(commands);

        try {
            execute(setMyCommands);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка регистрации команд: " + e.getMessage());
        }
    }

    private void handleMessage(long chatId, String text, UserSession session) {
        if (text.equals("/start")) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            sendDictionaryList(chatId);
            return;
        } else if (text.equals("/dictionary")) {
            handleDictionaryCommand(chatId, session);
            return;
        } else if (text.equals("/mode")) {
            handleModeCommand(chatId, session);
            return;
        }

        switch (session.getState()) {
            case CHOOSING_DICTIONARY -> sendDictionaryList(chatId);
            case CHOOSING_MODE -> sendModeSelection(chatId, session.getDictionary().getName());
            case TRAINING -> handleTrainingAnswer(chatId, text, session);
        }
    }

    private void handleDictionaryCommand(long chatId, UserSession session) {
        if (session.getState() == AppState.TRAINING) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            session.setTrainer(null);
        }

        sendDictionaryList(chatId);
    }

    private void handleModeCommand(long chatId, UserSession session) {
        if (session.getState() == AppState.TRAINING) {
            session.setState(AppState.CHOOSING_MODE);
            session.setTrainer(null);
        }

        if (session.getDictionary() == null) {
            sendText(chatId, "Сначала выберите словарь.");
            sendDictionaryList(chatId);
        } else {
            sendModeSelection(chatId, session.getDictionary().getName());
        }
    }

    private void handleCallback(long chatId, String data, UserSession session) {
        if (data.startsWith(Callbacks.DICT_PREFIX)) {
            int index = Integer.parseInt(data.substring(Callbacks.DICT_PREFIX.length()));
            Dictionary dictionary = dictionaryManager.loadDictionaryByIndex(index);
            session.setDictionary(dictionary);
            session.setState(AppState.CHOOSING_MODE);
            sendModeSelection(chatId, dictionary.getName());

        } else if (data.startsWith(Callbacks.MODE_PREFIX)) {
            String mode = data.substring(Callbacks.MODE_PREFIX.length());
            AbstractWordTrainer trainer = switch (mode) {
                case "definition" -> new DefinitionTrainer(session.getDictionary());
                case "russian" -> new RusToEngTrainer(session.getDictionary());
                default -> null;
            };
            if (trainer == null) return;

            session.setTrainer(trainer);
            session.setState(AppState.TRAINING);
            sendNextQuestion(chatId, session);

        } else if (data.equals(Callbacks.RETRY_WRONG)) {
            session.getTrainer().resetWithWrongOnly();
            sendNextQuestion(chatId, session);

        } else if (data.equals(Callbacks.RETRY_ALL)) {
            session.getTrainer().resetAll();
            sendNextQuestion(chatId, session);

        } else if (data.equals(Callbacks.BACK_TO_MENU)) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            sendDictionaryList(chatId);

        } else if (data.startsWith(Callbacks.LISTEN_PREFIX)) {
            String word = data.substring(Callbacks.LISTEN_PREFIX.length());
            File audio = audioService.getAudio(word);
            if (audio != null) {
                try {
                    SendVoice sendVoice = new SendVoice();
                    sendVoice.setChatId(String.valueOf(chatId));
                    sendVoice.setVoice(new InputFile(audio));
                    execute(sendVoice);
                } catch (TelegramApiException e) {
                    System.err.println("Ошибка отправки аудио: " + e.getMessage());
                }
            } else {
                sendText(chatId, "Не удалось загрузить аудио");
            }
        } else if (data.equals(Callbacks.UPLOAD_DICTIONARY)) {
            sendText(chatId, "Пришлите .txt файл в формате: первая строка — название словаря, далее\n" +
                    "  слова в формате english = russian = definition");

            session.setState(AppState.UPLOADING_DICTIONARY);
        }
    }

    private void handleTrainingAnswer(long chatId, String text, UserSession session) {
        if (text.equalsIgnoreCase("/menu")) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            sendDictionaryList(chatId);
            return;
        }

        AbstractWordTrainer trainer = session.getTrainer();

        Word currentWord = trainer.getCurrentWord();
        String result = trainer.handleAnswer(text);

        if (trainer.isFinished()) {
            sendFinishMenu(session, chatId, result + "\n\n" + trainer.getResultText());
        } else {
            sendWithListenButton(chatId, result, currentWord.getEnglish());
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
            button.setCallbackData(Callbacks.DICT_PREFIX + i);
            rows.add(List.of(button));
        }

        InlineKeyboardButton updDict = new InlineKeyboardButton("📥 Загрузить словарь");
        updDict.setCallbackData(Callbacks.UPLOAD_DICTIONARY);
        rows.add(List.of(updDict));

        sendWithKeyboard(chatId, "📚 Выберите словарь:", rows);
    }

    private void sendModeSelection(long chatId, String dictionaryName) {
        InlineKeyboardButton byDefinition = new InlineKeyboardButton("По определению на английском");
        byDefinition.setCallbackData(Callbacks.MODE_PREFIX + "definition");

        InlineKeyboardButton byRussian = new InlineKeyboardButton("По слову на русском");
        byRussian.setCallbackData(Callbacks.MODE_PREFIX + "russian");

        InlineKeyboardButton backToMenu = new InlineKeyboardButton("Выбрать другой словарь");
        backToMenu.setCallbackData(Callbacks.BACK_TO_MENU);

        sendWithKeyboard(chatId, "Вы выбрали: " + dictionaryName + "\n\nВыберите режим:",
                List.of(List.of(byDefinition), List.of(byRussian), List.of(backToMenu)));
    }

    private void sendFinishMenu(UserSession session, long chatId, String text) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (session.getTrainer().existsMoreWords()) {
            InlineKeyboardButton retryWrong = new InlineKeyboardButton("Повторить неправильные");
            retryWrong.setCallbackData(Callbacks.RETRY_WRONG);
            keyboard.add(List.of(retryWrong));
        }

        InlineKeyboardButton retryAll = new InlineKeyboardButton("Повторить всё заново");
        retryAll.setCallbackData(Callbacks.RETRY_ALL);
        keyboard.add(List.of(retryAll));

        InlineKeyboardButton backToMenu = new InlineKeyboardButton("Выбрать другой словарь");
        backToMenu.setCallbackData(Callbacks.BACK_TO_MENU);
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

    private void sendWithListenButton(long chatId, String text, String word) {
        InlineKeyboardButton btn = new InlineKeyboardButton("🔊 Произнести слово");
        btn.setCallbackData(Callbacks.LISTEN_PREFIX + word);
        sendWithKeyboard(chatId, text, List.of(List.of(btn)));
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
