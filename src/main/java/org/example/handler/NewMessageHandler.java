package org.example.handler;

import org.example.handler.base.BaseHandler;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.PhotoSize;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.example.handler.MediaHandler;
import org.example.handler.advertisement.AdvertisementHandler;
import org.example.handler.user.UserPanelHandler;
import org.example.handler.user.AdminPanelHandler;
import org.example.handler.language.LanguageHandler;
import org.example.service.MessageService;
import org.example.utils.ResourceBundleManager;
import static org.example.keyboardUtils.LanguageKeyboards.*;
import static org.example.keyboardUtils.SubscriptionKeyboards.*;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

public class NewMessageHandler extends BaseHandler {
    private final Map<Status, BiFunction<Message, User, Object>> handlers;
    private final AdvertisementHandler advertisementHandler;
    private final UserPanelHandler userPanelHandler;
    private final AdminPanelHandler adminPanelHandler;
    private final LanguageHandler languageHandler;
    private final MediaHandler mediaHandler;

    public NewMessageHandler() {
        this.advertisementHandler = new AdvertisementHandler();
        this.userPanelHandler = new UserPanelHandler();
        this.adminPanelHandler = new AdminPanelHandler();
        this.languageHandler = new LanguageHandler();
        this.mediaHandler = new MediaHandler();
        
        this.handlers = new EnumMap<>(Status.class);
        initializeHandlers();
    }

    private void initializeHandlers() {
        handlers.put(Status.START, this::handleStart);
        handlers.put(Status.WAITING_FOR_LANGUAGE_SELECTION, languageHandler::handle);
//        handlers.put(Status.WAITING_FOR_CHANNEL_VERIFICATION, this::handleChannelVerification);
        
        // User and Admin Panels
        handlers.put(Status.USER_PANEL, userPanelHandler::handle);
        handlers.put(Status.ADMIN_PANEL, adminPanelHandler::handle);

        // Advertisement Flow
        handlers.put(Status.WAITING_FOR_AD_TEXT, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_PRICE, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_LOCATION, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_DESCRIPTION, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_CONTACT, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_PHOTO, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_CONFIRMATION, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_STATISTICS, advertisementHandler::handle);
        handlers.put(Status.WAITING_FOR_AD_STATUS, advertisementHandler::handle);
    }

    private boolean isPanelSwitchMessage(String text, User user) {
        if (text == null) return false;
        String userPanelText = ResourceBundleManager.getMessage("button.user_panel", user.getLanguage());
        String adminPanelText = ResourceBundleManager.getMessage("button.admin_panel", user.getLanguage());
        return text.equals(userPanelText) || text.equals(adminPanelText);
    }

    public Object handleMessage(Message message) {
        try {
            Long chatId = message.getChatId();
            String text = message.getText();
            User user = UserService.getByChatId(message);
            
            // Handle panel switching before anything else
            if (isPanelSwitchMessage(text, user)) {
                if (text.equals(ResourceBundleManager.getMessage("button.user_panel", user.getLanguage()))) {
                    user.setStatus(Status.USER_PANEL);
                    UserService.update(user);
                    return userPanelHandler.handle(message, user);
                } else if (text.equals(ResourceBundleManager.getMessage("button.admin_panel", user.getLanguage())) && user.isAdmin()) {
                    user.setStatus(Status.ADMIN_PANEL);
                    UserService.update(user);
                    return adminPanelHandler.handle(message, user);
                }
            }
            
            // Handle /start command - always process this first
            if ("/start".equals(text)) {
                // Clean up any temporary advertisement data if it exists
                if (user.getStatus().name().startsWith("WAITING_FOR_AD_")) {
                    advertisementHandler.removeTempAd(chatId);
                    user.setStatus(Status.START);
                    UserService.update(user);
                    MediaHandler.removeTempAd(chatId);
                }
                
                // Set user status based on admin status
                if (user.isAdmin()) {
                    user.setStatus(Status.ADMIN_PANEL);
                    UserService.update(user);
                    return adminPanelHandler.handle(message, user);
                } else {
                    user.setStatus(Status.START);
                    UserService.update(user);
                    return handleStart(message, user);
                }
            }
            
            // Handle advertisement flow
            if (user.getStatus().name().startsWith("WAITING_FOR_AD_")) {
                // Handle photo uploads
                if (message.hasPhoto() && 
                    (user.getStatus() == Status.WAITING_FOR_AD_PHOTO || 
                     user.getStatus() == Status.WAITING_FOR_AD_CONFIRMATION)) {
                    return mediaHandler.handleMedia(message, user);
                }
                
                // Handle advertisement steps
                return advertisementHandler.handle(message, user);
            }
            
            // Get the appropriate handler for the current status
            BiFunction<Message, User, Object> handler = handlers.get(user.getStatus());
            if (handler != null) {
                return handler.apply(message, user);
            }
            
            // If no specific handler found, don't respond to any messages
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return MessageService.createMessage(message.getChatId(), 
                    "Xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring.");
        }
    }
    
    private Object handleStart(Message message, User user) {
        // Clear any temporary data first
        Long chatId = message.getChatId();
        if (user.getStatus().name().startsWith("WAITING_FOR_AD_")) {
            advertisementHandler.removeTempAd(chatId);
            mediaHandler.removeTempAd(chatId);
        }
        
        // Set user to language selection status
        user.setStatus(Status.WAITING_FOR_LANGUAGE_SELECTION);
        UserService.update(user);

        // Show language selection
        return MessageService.createMessage(
            chatId,
            "Iltimos, tilni tanlang / Пожалуйста, выберите язык",
            languageKeyboard()
        );
    }
    
    private Object handleChannelVerification(Message message, User user) {
        Long chatId = message.getChatId();
        String language = user.getLanguage() != null ? user.getLanguage() : "uz";

        try {
            // For now, we'll just move the user to the user panel
            // In a real implementation, you would verify if the user has joined the required channels
            user.setStatus(Status.USER_PANEL);
            UserService.update(user);

            // Get the welcome message in the user's language
            String welcomeMessage = ResourceBundleManager.getMessage("welcome", language);

            // Redirect to the user panel
            return userPanelHandler.handle(message, user);

        } catch (Exception e) {
            e.printStackTrace();
            return MessageService.createMessage(
                chatId,
                ResourceBundleManager.getMessage("error_occurred", language) + "\n" +
                ResourceBundleManager.getMessage("try_again", language)
            );
        }
    }
}
