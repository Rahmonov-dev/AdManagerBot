package org.example.handler.user;

import org.example.handler.MediaHandler;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.handler.base.BaseHandler;
import org.example.service.UserService;
import org.example.service.MessageService;
import org.example.utils.ResourceBundleManager;
import org.example.handler.advertisement.AdvertisementHandler;

import static org.example.keyboardUtils.AdminUserKeyboard.*;
import static org.example.keyboardUtils.LanguageKeyboards.languageKeyboard;
import static org.example.keyboardUtils.UserKeyboard.*;

public class UserPanelHandler extends BaseHandler {
    private final AdvertisementHandler advertisementHandler;
    private final MediaHandler mediaHandler;

    public UserPanelHandler() {
        this.advertisementHandler = new AdvertisementHandler();
        this.mediaHandler = new MediaHandler();
    }

    public Object handle(Message message, User user) {
        try {
            String text = message.getText();
            Long chatId = message.getChatId();
            String backButtonText = ResourceBundleManager.getMessage("button.back", user.getLanguage());
            String sendAdButtonText = ResourceBundleManager.getMessage("button.send_ad", user.getLanguage());
            String cancelButtonText = "❌ " + ResourceBundleManager.getMessage("button.cancel", user.getLanguage());

            // Check if user is in advertisement flow
            if (user.getStatus().name().startsWith("WAITING_FOR_AD_")) {
                // Delegate to advertisement handler for all advertisement-related states
                return advertisementHandler.handle(message, user);
            }

            // Check if user clicked on "Back" button
            if (text != null && text.equals(backButtonText)) {
                removeTempData(chatId);
                if (user.isAdmin()) {
                    user.setStatus(Status.ADMIN_PANEL);
                    UserService.update(user);
                    return MessageService.createMessage(
                            chatId,
                            ResourceBundleManager.getMessage("admin_panel", user.getLanguage()),
                            createAdminUserKeyboard(user)
                    );
                } else {
                    user.setStatus(Status.WAITING_FOR_LANGUAGE_SELECTION);
                    UserService.update(user);
                    return MessageService.createMessage(
                            chatId,
                            ResourceBundleManager.getMessage("user_panel", user.getLanguage()),
                            languageKeyboard()
                    );
                }
            }

            // Check if user clicked on "Reklama berish" button
            else if (text != null && text.equals(sendAdButtonText)) {
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

            //check if user clicked on "Ad Status" button
            else if (text != null && text.equals(ResourceBundleManager.getMessage("button.ad_status", user.getLanguage()))) {
                user.setStatus(Status.WAITING_FOR_AD_STATUS);
                UserService.update(user);
                return advertisementHandler.handle(message, user);
            }
            // check if user clicked on "Ad Statistics" button
            else if (text != null && text.equals(ResourceBundleManager.getMessage("button.statistics", user.getLanguage()))) {
                user.setStatus(Status.WAITING_FOR_AD_STATISTICS);
                UserService.update(user);
                return advertisementHandler.handle(message, user);
            }
            //check if user clicked on "Price" button
            else if (text != null && text.equals(ResourceBundleManager.getMessage("button_ad_price", user.getLanguage()))) {
                return MessageService.createMessage(
                        chatId,
                        ResourceBundleManager.getMessage("prices?????????????????", user.getLanguage())
                );
            }
            // check if user clicked on "Cancel" button
            else if (text != null && text.equals(cancelButtonText)) {
                removeTempData(chatId);
                return MessageService.createMessage(
                        chatId,
                        ResourceBundleManager.getMessage("button.cancel", user.getLanguage())
                );
            }

            // Default behavior - show user panel
            user.setStatus(Status.USER_PANEL);
            UserService.update(user);
            return MessageService.createMessage(
                    chatId,
                    ResourceBundleManager.getMessage("user_panel", user.getLanguage()),
                    createUserKeyboard(user)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return MessageService.createMessage(
                    message.getChatId(),
                    "Xatolik yuz berdi. Iltimos, qaytadan urinib ko'ring."
            );
        }
    }
}
