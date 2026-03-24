package com.vladimir.wordtrainer.bot.handler;

import com.vladimir.wordtrainer.bot.Callbacks;
import com.vladimir.wordtrainer.bot.MessageSender;
import com.vladimir.wordtrainer.model.Dictionary;
import com.vladimir.wordtrainer.model.Word;
import com.vladimir.wordtrainer.service.AudioService;
import com.vladimir.wordtrainer.service.TrainingService;
import com.vladimir.wordtrainer.service.trainer.Trainer;
import com.vladimir.wordtrainer.session.AppState;
import com.vladimir.wordtrainer.session.UserSession;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class TrainingHandler {
    private final MessageSender messageSender;
    private final AudioService audioService;
    private final TrainingService trainingService;

    public TrainingHandler(MessageSender messageSender, AudioService audioService, TrainingService trainingService) {
        this.messageSender = messageSender;
        this.audioService = audioService;
        this.trainingService = trainingService;
    }

    public void handleMessage(long chatId, String text, UserSession session) {
        if (text.equalsIgnoreCase("/menu")) {
            session.setState(AppState.CHOOSING_DICTIONARY);
            return;
        }

        Trainer trainer = session.getTrainer();
        Word currentWord = trainer.getCurrentWord();
        String result = trainer.handleAnswer(text);

        if (trainer.isFinished()) {
            sendFinishMenu(session, chatId, result + "\n\n" + trainer.getResultText());
        } else {
            messageSender.sendWithListenButton(chatId, result, currentWord.getId());
            sendNextQuestion(chatId, session);
        }
    }

    public void sendNextQuestion(long chatId, UserSession session) {
        String question = session.getTrainer().getNextQuestion();
        if (question == null) {
            sendFinishMenu(session, chatId, session.getTrainer().getResultText());
            return;
        }
        messageSender.sendText(chatId, session.getTrainer().getProgressText() + "\n\n" + question);
    }

    private void sendFinishMenu(UserSession session, long chatId, String text) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        if (session.getTrainer().existsMoreWords()) {
            InlineKeyboardButton retryWrong = new InlineKeyboardButton(Callbacks.RETRY_WRONG.buttonText());
            retryWrong.setCallbackData(Callbacks.RETRY_WRONG.callback());
            keyboard.add(List.of(retryWrong));
        }

        InlineKeyboardButton retryAll = new InlineKeyboardButton(Callbacks.RETRY_ALL.buttonText());
        retryAll.setCallbackData(Callbacks.RETRY_ALL.callback());
        keyboard.add(List.of(retryAll));

        InlineKeyboardButton backToMenu = new InlineKeyboardButton(Callbacks.BACK_TO_MENU.buttonText());
        backToMenu.setCallbackData(Callbacks.BACK_TO_MENU.callback());
        keyboard.add(List.of(backToMenu));

        messageSender.sendWithKeyboard(chatId, "♻️ " + text + "\n\nЧто делаем дальше?", keyboard);
    }

    public void handleListenCallback(long chatId, Word word) {
        java.io.File audio = audioService.getAudio(word);
        if (audio != null) {
            messageSender.sendAudio(chatId, audio);
        } else {
            messageSender.sendText(chatId, "Не удалось загрузить аудио");
        }
    }

    public void handleModeSelected(long chatId, String mode, UserSession session) {
        Trainer trainer = trainingService.createTrainer(mode, session.getDictionary());
        if (trainer == null) return;
        session.setTrainer(trainer);
        session.setState(AppState.TRAINING);
    }
}