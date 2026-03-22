package com.vladimir.wordtrainer.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.File;
import java.util.List;

public interface MessageSender {
    void sendText(long chatId, String text);
    void sendWithKeyboard(long chatId, String text, List<List<InlineKeyboardButton>> rows);
    void sendWithListenButton(long chatId, String text, String word);
    void sendAudio(long chatId, File audio);
}
