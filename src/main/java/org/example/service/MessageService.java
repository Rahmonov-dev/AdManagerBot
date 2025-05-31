package org.example.service;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

public class MessageService {
    
    public static SendMessage createMessage(Long chatId, String text) {
        return createMessage(chatId.toString(), text, null);
    }

    public static SendMessage createMessage(String chatId, String text) {
        return createMessage(chatId, text, null);
    }

    public static SendMessage createMessage(Long chatId, String text, ReplyKeyboard replyMarkup) {
        return createMessage(chatId.toString(), text, replyMarkup);
    }

    public static SendMessage createMessage(String chatId, String text, ReplyKeyboard replyMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);
        if (replyMarkup != null) {
            message.setReplyMarkup(replyMarkup);
        }
        return message;
    }
}
