package org.example;

import org.example.config.Config;
import org.example.handler.MessageHandler;
import org.example.handler.QueryHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);

    public TelegramBot() {
        super(Config.get("BOT_TOKEN"));
        logger.info("TelegramBot initialized with token from Config");
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Received update: {}", update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            BotApiMethod<?> response = MessageHandler.handleMessage(update.getMessage());
            if (response != null) {
                try {
                    logger.info("Executing response: {}", response);
                    execute(response);
                } catch (TelegramApiException e) {
                    logger.error("Failed to execute response for update: {}", update, e);
                }
            } else {
                logger.warn("No response generated for update: {}", update);
            }
        } else if (update.hasCallbackQuery()) {
            BotApiMethod<?> response = QueryHandler.handleQuery(update.getCallbackQuery());
            if (response != null) {
                try {
                    logger.info("Executing response: {}", response);
                    execute(response);
                } catch (TelegramApiException e) {
                    logger.error("Failed to execute response for update: {}", update, e);
                }
            }
        } else {
            logger.warn("Update does not contain a valid message with text: {}", update);
        }
    }

    @Override
    public String getBotUsername() {
        return Config.get("BOT_USERNAME");
    }
}