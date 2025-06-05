package org.example.handler.advertisement;

import org.telegram.telegrambots.meta.api.objects.EntityType;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

import org.example.model.Advertisement;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.handler.base.BaseHandler;
import org.example.service.UserService;
import org.example.service.MessageService;
import org.example.utils.ResourceBundleManager;
import org.example.handler.MediaHandler;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.example.keyboardUtils.UserKeyboard.*;

public class AdvertisementHandler extends BaseHandler {
    private final Map<Status, StateHandler> stateHandlers;
    private final Map<Long, Advertisement> tempAds;
    private final MediaHandler mediaHandler;

    public AdvertisementHandler() {
        this.stateHandlers = new EnumMap<>(Status.class);
        this.tempAds = new ConcurrentHashMap<>();
        this.mediaHandler = new MediaHandler();
        registerHandlers();
    }

    private void registerHandlers() {
        stateHandlers.put(Status.WAITING_FOR_AD_TEXT, this::handleText);
        stateHandlers.put(Status.WAITING_FOR_AD_PRICE, this::handlePrice);
        stateHandlers.put(Status.WAITING_FOR_AD_LOCATION, this::handleLocation);
        stateHandlers.put(Status.WAITING_FOR_AD_DESCRIPTION, this::handleDescription);
        stateHandlers.put(Status.WAITING_FOR_AD_CONTACT, this::handleContact);
        stateHandlers.put(Status.WAITING_FOR_AD_PHOTO, this::handlePhoto);
        stateHandlers.put(Status.WAITING_FOR_AD_CONFIRMATION, this::handleConfirmation);
        stateHandlers.put(Status.WAITING_FOR_AD_STATISTICS, this::handleStatistics);
        stateHandlers.put(Status.WAITING_FOR_AD_STATUS, this::handleStatus);
    }

    private Object handleStatus(Message message, User user) {
        Long chatId = message.getChatId();
        
        // Always reset to user panel status after showing status
        user.setStatus(Status.USER_PANEL);
        UserService.update(user);
        
        // Get the ad from temporary storage
        Advertisement userAd = getTempAd(chatId);

        // Check if ad exists and has any content
        if (userAd != null && hasAdContent(userAd)) {
            // User has a valid ad, return formatted message with status
            String status = userAd.isConfirmed() ? "✅ Tasdiqlangan" : "⏳ Ko'rib chiqilmoqda";
            String response = "📊 Reklama holati\n\n" +
                           userAd.toFormattedString() + "\n\n" +
                           "🔄 Holat: " + status;
            
            // If ad has a photo, send it with the caption
            if (userAd.getPhotoFileId() != null && !userAd.getPhotoFileId().isEmpty()) {
                return MessageService.createPhotoMessage(
                    chatId,
                    userAd.getPhotoFileId(),
                    response,
                    createUserKeyboard(user)
                );
            }
            
            // Otherwise, send just the text
            return MessageService.createMessage(
                chatId,
                response,
                createUserKeyboard(user)
            );
        }

        // No ad found for user
        return MessageService.createMessage(
                chatId,
                "ℹ️ Sizda hozircha e'lonlar mavjud emas.\n" +
                "Yangi e'lon qo'shish uchun \"📢 Reklama berish\" tugmasini bosing.",
                createUserKeyboard(user)
        );
    }
    private boolean hasAdContent(Advertisement ad) {
        return (ad.getProductName() != null && !ad.getProductName().trim().isEmpty());
    }

    private Object handleStatistics(Message message, User user) {
        String text = message.getText();
        Long chatId = message.getChatId();

        // Check if back button was pressed
        String backButtonText = ResourceBundleManager.getMessage("button_back", user.getLanguage());

        if (text != null && text.equals(backButtonText)) {
            user.setStatus(Status.USER_PANEL);
            UserService.update(user);
            return MessageService.createMessage(
                    chatId,
                    ResourceBundleManager.getMessage("user_panel", user.getLanguage()),
                    createUserKeyboard(user)
            );
        }

        // Show statistics options
        return MessageService.createMessage(chatId,
                ResourceBundleManager.getMessage("choose_statistics", user.getLanguage()),
                createStatisticsKeyboard(user));
    }

    public Object handle(Message message, User user) {
        // Check for cancellation
        String cancelText = "❌ " + ResourceBundleManager.getMessage("button.cancel", user.getLanguage());
        if (cancelText.equals(message.getText())) {
            return handleCancellation(message, user);
        }

        // Ensure temp ad exists for the user if they're in ad creation flow
        if (user.getStatus().name().startsWith("WAITING_FOR_AD_")) {
            Long chatId = message.getChatId();
            Advertisement ad = tempAds.get(chatId);
            if (ad == null) {
                // Try to get from MediaHandler's temp storage
                ad = MediaHandler.getTempAd(chatId);
                if (ad != null) {
                    tempAds.put(chatId, ad);
                } else {
                    // Create new ad if none exists
                    ad = new Advertisement();
                    tempAds.put(chatId, ad);
                    MediaHandler.saveTempAd(chatId, ad);
                }
            }
        }

        // Handle normal state flow
        StateHandler handler = stateHandlers.get(user.getStatus());
        return handler != null ? handler.handle(message, user) : getErrorMessage(message.getChatId(), user);
    }

    public void removeTempAd(Long chatId) {
        if (chatId != null) {
            tempAds.remove(chatId);
            // Also remove from MediaHandler
            MediaHandler.removeTempAd(chatId);
        }
    }

    private Object handleCancellation(Message message, User user) {
        Long chatId = message.getChatId();
        tempAds.remove(chatId);
        user.setStatus(Status.USER_PANEL);
        UserService.update(user);

        return MessageService.createMessage(
                chatId,
                ResourceBundleManager.getMessage("ad.cancelled", user.getLanguage())
        );
    }

    private Object handleText(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();

        // Create or update ad with product name
        Advertisement ad = getTempAd(chatId);
        if (ad == null) {
            ad = new Advertisement();
        }
        ad.setProductName(text);
        saveTempAd(chatId, ad);

        user.setStatus(Status.WAITING_FOR_AD_PRICE);
        UserService.update(user);

        return MessageService.createMessage(chatId,
                ResourceBundleManager.getMessage("ad.ask_price", user.getLanguage()));
    }

    private Object handlePrice(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();

        // Update ad with price
        Advertisement ad = getTempAd(chatId);
        if (ad == null) {
            return handleCancellation(message, user);
        }
        ad.setPrice(text);
        saveTempAd(chatId, ad);

        user.setStatus(Status.WAITING_FOR_AD_LOCATION);
        UserService.update(user);

        return MessageService.createMessage(chatId,
                ResourceBundleManager.getMessage("ad.ask_location", user.getLanguage()));
    }

    private Object handleLocation(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();

        // Update ad with location
        Advertisement ad = getTempAd(chatId);
        if (ad == null) {
            return handleCancellation(message, user);
        }
        ad.setLocation(text);
        saveTempAd(chatId, ad);

        user.setStatus(Status.WAITING_FOR_AD_DESCRIPTION);
        UserService.update(user);

        return MessageService.createMessage(chatId,
                ResourceBundleManager.getMessage("ad.ask_description", user.getLanguage()));
    }

    private Object handleDescription(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();

        // Update ad with description
        Advertisement ad = getTempAd(chatId);
        if (ad == null) {
            return handleCancellation(message, user);
        }
        ad.setDescription(text);
        saveTempAd(chatId, ad);

        user.setStatus(Status.WAITING_FOR_AD_PHOTO);
        UserService.update(user);

        return MessageService.createMessage(chatId,
                ResourceBundleManager.getMessage("ad.ask_photo", user.getLanguage()));
    }

    private Object handleContact(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();

        // Update ad with contact info
        Advertisement ad = getTempAd(chatId);
        if (ad == null) {
            return handleCancellation(message, user);
        }
        ad.setContactInfo(text);
        saveTempAd(chatId, ad);

        user.setStatus(Status.WAITING_FOR_AD_CONFIRMATION);
        UserService.update(user);

        // Show ad preview for confirmation
        String confirmationMessage = ad.toFormattedString() + "\n\n" +
                ResourceBundleManager.getMessage("ad.confirm_ad", user.getLanguage());

        // Create reply keyboard with confirm/cancel buttons
        ReplyKeyboardMarkup replyMarkup = new ReplyKeyboardMarkup();
        replyMarkup.setResizeKeyboard(true);
        replyMarkup.setOneTimeKeyboard(true);

        String confirmText = "✅ " + ResourceBundleManager.getMessage("button.confirm", user.getLanguage());
        String cancelText = "❌ " + ResourceBundleManager.getMessage("button.cancel", user.getLanguage());

        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(confirmText);
        row.add(cancelText);
        keyboard.add(row);
        replyMarkup.setKeyboard(keyboard);

        // If there's a photo, send it with the caption and buttons
        if (ad.getPhotoFileId() != null && !ad.getPhotoFileId().isEmpty()) {
            return MessageService.createPhotoMessage(
                    chatId,
                    ad.getPhotoFileId(),
                    confirmationMessage,
                    replyMarkup
            );
        }

        // Otherwise, just send the text message with buttons
        return MessageService.createMessage(chatId, confirmationMessage, replyMarkup);
    }

    private Object handlePhoto(Message message, User user) {
        // This method is called when user is in WAITING_FOR_AD_PHOTO state
        // and sends a message (not a photo)
        Long chatId = message.getChatId();

        // If user sends text instead of photo, ask them to send a photo
        if (message.hasText()) {
            return MessageService.createMessage(chatId,
                    ResourceBundleManager.getMessage("ad.ask_photo_again", user.getLanguage()));
        }

        // If we get here, it's not a photo, so cancel the operation
        return handleCancellation(message, user);
    }


    public void saveTempAd(Long chatId, Advertisement ad) {
        if (chatId != null && ad != null) {
            tempAds.put(chatId, ad);
            // Also save to MediaHandler's temp storage
            MediaHandler.saveTempAd(chatId, ad);
        }
    }


    public Advertisement getTempAd(Long chatId) {
        if (chatId == null) return null;

        // First try to get from local temp storage
        Advertisement ad = tempAds.get(chatId);

        // If not found, try to get from MediaHandler
        if (ad == null) {
            ad = MediaHandler.getTempAd(chatId);
            if (ad != null) {
                // Cache it locally for future use
                tempAds.put(chatId, ad);
            }
        }

        return ad;
    }

    private Object handleConfirmation(Message message, User user) {
        Long chatId = message.getChatId();
        
        // Get the ad from temporary storage
        Advertisement ad = getTempAd(chatId);
        if (ad == null) {
            return handleCancellation(message, user);
        }
        
        // Save the ad (initially unconfirmed - waiting for admin approval)
        ad.setConfirmed(false); // Admin needs to confirm the ad
        saveTempAd(chatId, ad);
        
        // Update user status
        user.setStatus(Status.USER_PANEL);
        UserService.update(user);
        
        // Create response message
        String response = "✅ E'loningiz qabul qilindi!\n\n" +
                       "📊 Reklama holatini ko'rish uchun \"📊 Reklama holati\" tugmasini bosing.\n" +
                       "❗ Iltimos, reklama admin tomonidan ko'rib chiqilishini kuting.";
        
        // TODO: Notify admin about new ad for approval
        
        return MessageService.createMessage(chatId, response, createUserKeyboard(user));
    }
}
