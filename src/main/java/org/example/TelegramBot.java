package org.example;

import org.example.config.Config;
import org.example.handler.NewMessageHandler;
import org.example.handler.QueryHandler;
import org.example.model.User;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

public class TelegramBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(TelegramBot.class);
    private final NewMessageHandler messageHandler;
    private static volatile TelegramBot instance;

    public TelegramBot() {
        super(Config.get("BOT_TOKEN"));
        this.messageHandler = new NewMessageHandler();
        QueryHandler.setBot(this);
        logger.info("TelegramBot initialized with token from Config");
    }
    public static TelegramBot getInstance() {
        if (instance == null) {
            synchronized (TelegramBot.class) {
                if (instance == null) {
                    instance = new TelegramBot();
                }
            }
        }
        return instance;
    }

    @Override
    public void onUpdateReceived(Update update) {
        logger.info("Received update: {}", update);
        if (update.hasMessage() && update.getMessage().hasText()) {
            Object response = messageHandler.handleMessage(update.getMessage());
            executeResponse(update, response);
        } else if (update.hasCallbackQuery()) {
            Object response = QueryHandler.handleQuery(update.getCallbackQuery());
            if (response != null) {
                try {
                    logger.info("Executing response: {}", response);
                    if (response instanceof SendMessage) {
                        execute((SendMessage) response);
                    } else if (response instanceof SendPhoto) {
                        execute((SendPhoto) response);
                    } else if (response instanceof BotApiMethod) {
                        execute((BotApiMethod<?>) response);
                    } else {
                        logger.warn("Unsupported response type from QueryHandler: {}", 
                            response.getClass().getName());
                    }
                } catch (TelegramApiException e) {
                    logger.error("Failed to execute response for update: {}", update, e);
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            List<User> users = UserRepository.readUsers();
            User user = users.stream()
                    .filter(u -> u.getId().equals(update.getMessage().getChatId()))
                    .findFirst()
                    .orElse(null);
            assert user != null;
            Object response = messageHandler.handleMessage(update.getMessage());
            executeResponse(update, response);
        } else {
            logger.warn("Update does not contain a valid message with text: {}", update);
        }
    }

    private void executeResponse(Update update, Object response) {
        if (response != null) {
            try {
                logger.info("Executing response: {}", response);
                if (response instanceof SendMessage) {
                    SendMessage sendMessage = (SendMessage) response;
                    logger.info("Sending message - ChatId: {}, Text: {}", 
                        sendMessage.getChatId(), sendMessage.getText());
                    execute(sendMessage);
                } else if (response instanceof SendPhoto) {
                    SendPhoto sendPhoto = (SendPhoto) response;
                    logger.info("Sending photo - ChatId: {}, Caption: {}", 
                        sendPhoto.getChatId(), sendPhoto.getCaption());
                    execute(sendPhoto);
                } else if (response instanceof BotApiMethod) {
                    logger.info("Executing BotApiMethod: {}", response.getClass().getSimpleName());
                    execute((BotApiMethod<?>) response);
                } else {
                    logger.warn("Unsupported response type: {}", response.getClass().getName());
                }
            } catch (TelegramApiException e) {
                logger.error("Failed to execute response for update: {}", update, e);
            }
        } else {
            logger.warn("No response generated for update: {}", update);
        }
    }

    @Override
    public String getBotUsername() {
        return Config.get("BOT_USERNAME");
    }
}