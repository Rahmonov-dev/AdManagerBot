package org.example.handler.user;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.example.handler.advertisement.AdvertisementHandler;
import org.example.handler.MediaHandler;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.handler.base.BaseHandler;
import org.example.service.UserService;
import org.example.service.MessageService;
import org.example.utils.ResourceBundleManager;


import static org.example.keyboardUtils.AdminUserKeyboard.*;
import static org.example.keyboardUtils.UserKeyboard.createUserKeyboard;

public class AdminPanelHandler extends BaseHandler {
    private final AdvertisementHandler advertisementHandler;
    private final MediaHandler mediaHandler;
    
    public AdminPanelHandler() {
        this.advertisementHandler = new AdvertisementHandler();
        this.mediaHandler = new MediaHandler();
    }
    
    public Object handle(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();

        // Handle panel switching
        Object panelSwitch = handlePanelButton(message, user);
        if (panelSwitch != null) {
            return panelSwitch;
        }

        // Handle ad creation from admin panel
        Object adCreation = handleAdCreation(message, user);
        if (adCreation != null) {
            return adCreation;
        }


        // Default admin panel behavior
        return MessageService.createMessage(
                chatId,
                ResourceBundleManager.getMessage("admin_panel", user.getLanguage()),
                createAdminUserKeyboard(user)
        );
    }

    private BotApiMethod<Message> handleAdCreation(Message message, User user) {
        Long chatId = message.getChatId();
        String text = message.getText();


        String sendAdText = ResourceBundleManager.getMessage("button.send_ad", user.getLanguage());

        if (text != null && text.equals(sendAdText)) {
            // Clear any existing temp data
            advertisementHandler.removeTempAd(chatId);
            mediaHandler.removeTempAd(chatId);

            // Set status to start ad flow
            user.setStatus(Status.WAITING_FOR_AD_TEXT);
            UserService.update(user);

            // Ask for product name
            return MessageService.createMessage(
                    chatId,
                    ResourceBundleManager.getMessage("ad.ask_product_name", user.getLanguage())
            );
        }
        return null;
    }

    private BotApiMethod<Message> handlePanelButton(Message message, User user) {
        String userPanelText = ResourceBundleManager.getMessage("button.user_panel", user.getLanguage());
        String adminPanelText = ResourceBundleManager.getMessage("button.admin_panel", user.getLanguage());
        Long chatId = message.getChatId();
        String text = message.getText();
        if (text != null && text.contains(userPanelText)) {
            user.setStatus(Status.USER_PANEL);
            UserService.update(user);
            return MessageService.createMessage(
                    chatId,
                    ResourceBundleManager.getMessage("user_panel", user.getLanguage()),
                    createUserKeyboard(user)
            );
        } else if (text != null && text.contains(adminPanelText)) {
            user.setStatus(Status.ADMIN_PANEL);
            UserService.update(user);
            return MessageService.createMessage(
                    chatId,
                    ResourceBundleManager.getMessage("admin_panel", user.getLanguage()),
                    createAdminKeyboard(user)
            );
        }
        return null;
    }
}
