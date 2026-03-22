package com.vladimir.wordtrainer.bot;

import com.vladimir.wordtrainer.bot.handler.DictionaryManagementHandler;
import com.vladimir.wordtrainer.bot.handler.PreTrainingHandler;
import com.vladimir.wordtrainer.bot.handler.TrainingHandler;
import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.service.AudioService;
import com.vladimir.wordtrainer.service.DictionaryService;
import com.vladimir.wordtrainer.service.TrainingService;
import com.vladimir.wordtrainer.service.UserService;
import com.vladimir.wordtrainer.session.AppState;
import com.vladimir.wordtrainer.session.UserSession;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendVoice;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordTrainerBot extends TelegramLongPollingBot implements MessageSender {
    private final String botUsername;
    private final Map<Long, UserSession> sessions = new HashMap<>();
    private final UserService userService;

    private final TrainingHandler trainingHandler;
    private final PreTrainingHandler preTrainingHandler;
    private final DictionaryManagementHandler dictionaryManagementHandler;

    public WordTrainerBot(String botToken,
                          String botUsername,
                          DictionaryService dictionaryService,
                          UserService userService,
                          TrainingService trainingService,
                          AudioService audioService) {
        super(botToken);
        this.botUsername = botUsername;

        this.userService = userService;

        this.trainingHandler = new TrainingHandler(this, audioService, trainingService);
        this.preTrainingHandler = new PreTrainingHandler(this, dictionaryService);
        this.dictionaryManagementHandler = new DictionaryManagementHandler(this, dictionaryService);

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
            long chatId = update.getMessage().getChatId();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            session.setTelegramUserId(from.getId());
            registerUserIfNeeded(from, session);
            String text = update.getMessage().getText().trim();
            handleMessage(chatId, text, session);
        } else if (update.hasCallbackQuery()) {
            org.telegram.telegrambots.meta.api.objects.User from = update.getCallbackQuery().getFrom();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            String data = update.getCallbackQuery().getData();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            session.setTelegramUserId(from.getId());
            registerUserIfNeeded(from, session);
            handleCallback(chatId, data, session);
        } else if (update.hasMessage() && update.getMessage().hasDocument()) {
            long chatId = update.getMessage().getChatId();
            UserSession session = sessions.computeIfAbsent(chatId, id -> new UserSession());
            if (session.getState() == AppState.UPLOADING_DICTIONARY) {
                String fileId = update.getMessage().getDocument().getFileId();
                try {
                    File file = downloadFile(execute(new GetFile(fileId)));
                    dictionaryManagementHandler.handleFileUploaded(chatId, file, session);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    private void handleMessage(long chatId, String text, UserSession session) {
        if (text.equals("/start")) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            preTrainingHandler.sendDictionaryList(chatId, session);
            return;
        } else if (text.equals("/dictionary")) {
            preTrainingHandler.handleDictionaryCommand(chatId, session);
            return;
        } else if (text.equals("/mode")) {
            preTrainingHandler.handleModeCommand(chatId, session);
            return;
        }

        switch (session.getState()) {
            case CHOOSING_DICTIONARY -> preTrainingHandler.sendDictionaryList(chatId, session);
            case CHOOSING_MODE -> preTrainingHandler.sendModeSelection(chatId, session.getDictionary().getName());
            case TRAINING -> trainingHandler.handleMessage(chatId, text, session);
        }
    }

    private void handleCallback(long chatId, String data, UserSession session) {
        if (data.startsWith(Callbacks.DICT_PREFIX)) {
            Long dictionaryId = Long.parseLong(data.substring(Callbacks.DICT_PREFIX.length()));
            List<Word> words = preTrainingHandler.getDictionaryService().getWords(dictionaryId);

            Dictionary dictionary = new Dictionary(words, session.getAvailableDictionary().get(dictionaryId));
            session.setDictionary(dictionary);
            session.setState(AppState.CHOOSING_MODE);
            preTrainingHandler.sendModeSelection(chatId, dictionary.getName());

        } else if (data.startsWith(Callbacks.MODE_PREFIX)) {
            String mode = data.substring(Callbacks.MODE_PREFIX.length());
            trainingHandler.handleModeSelected(chatId, mode, session);
            trainingHandler.sendNextQuestion(chatId, session);

        } else if (data.equals(Callbacks.RETRY_WRONG)) {
            session.getTrainer().resetWithWrongOnly();
            trainingHandler.sendNextQuestion(chatId, session);

        } else if (data.equals(Callbacks.RETRY_ALL)) {
            session.getTrainer().resetAll();
            trainingHandler.sendNextQuestion(chatId, session);

        } else if (data.equals(Callbacks.BACK_TO_MENU)) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            preTrainingHandler.sendDictionaryList(chatId, session);

        } else if (data.startsWith(Callbacks.LISTEN)) {
            String word =  session.getTrainer().getCurrentWord().getEnglish();
            trainingHandler.handleListenCallback(chatId, word);

        } else if (data.equals(Callbacks.UPLOAD_DICTIONARY)) {
            dictionaryManagementHandler.handleUploadRequest(chatId, session);

        } else if (data.equals(Callbacks.DICT_VISIBILITY_PUBLIC) || data.equals(Callbacks.DICT_VISIBILITY_PRIVATE)) {
            dictionaryManagementHandler.handleVisibilityCallback(chatId, data, session);
            preTrainingHandler.sendDictionaryList(chatId, session);
        }
    }

    private void registerUserIfNeeded(org.telegram.telegrambots.meta.api.objects.User from, UserSession session) {
        if (!session.isExistsInRepository() && !userService.isUserExists(from.getId())) {
            userService.register(from.getId(), from.getUserName(), from.getFirstName(), from.getLastName());
            session.setExistsInRepository(true);
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

    @Override
    public void sendText(long chatId, String text) {
        try {
            execute(new SendMessage(String.valueOf(chatId), text));
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки: " + e.getMessage());
        }
    }

    @Override
    public void sendWithKeyboard(long chatId, String text, List<List<InlineKeyboardButton>> rows) {
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

    @Override
    public void sendWithListenButton(long chatId, String text, String word) {
        InlineKeyboardButton btn = new InlineKeyboardButton("🔊 Произнести слово");
        btn.setCallbackData(Callbacks.LISTEN);
        sendWithKeyboard(chatId, text, List.of(List.of(btn)));
    }

    @Override
    public void sendAudio(long chatId, File audio) {
        try {
            SendVoice sendVoice = new SendVoice();
            sendVoice.setChatId(String.valueOf(chatId));
            sendVoice.setVoice(new InputFile(audio));
            execute(sendVoice);
        } catch (TelegramApiException e) {
            System.err.println("Ошибка отправки аудио: " + e.getMessage());
        }
    }
}