package org.example.service;

import org.example.TelegramBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.example.TelegramBot.*;

public class MessageService {
    public static TelegramBot bot = getInstance();
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
    

    public static SendPhoto createPhotoMessage(Long chatId, String photoFileId, String caption, ReplyKeyboard replyMarkup) {
        return createPhotoMessage(chatId.toString(), photoFileId, caption, replyMarkup);
    }
    
    public static SendPhoto createPhotoMessage(String chatId, String photoFileId, String caption, ReplyKeyboard replyMarkup) {
        SendPhoto photoMessage = new SendPhoto();
        photoMessage.setChatId(chatId);
        photoMessage.setPhoto(new InputFile(photoFileId));
        photoMessage.setCaption(caption);
        if (replyMarkup != null) {
            photoMessage.setReplyMarkup(replyMarkup);
        }
        return photoMessage;
    }


}
