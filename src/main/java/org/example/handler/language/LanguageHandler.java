package org.example.handler.language;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.handler.base.BaseHandler;
import org.example.service.UserService;
import org.example.service.MessageService;
import org.example.utils.ResourceBundleManager;
import static org.example.keyboardUtils.LanguageKeyboards.*;
import static org.example.keyboardUtils.SubscriptionKeyboards.*;

public class LanguageHandler extends BaseHandler {

    public Object handle(Message message, User user) {
        String text = message.getText();
        Long chatId = message.getChatId();
        
        if (text != null) {
            if (text.contains("O'zbekcha")) {
                user.setLanguage("uz");
                user.setStatus(Status.WAITING_FOR_CHANNEL_VERIFICATION);
                UserService.update(user);
                return MessageService.createMessage(
                        chatId,
                        ResourceBundleManager.getMessage("ask_for_join", user.getLanguage()),
                        forceSubscribe(user)
                );
            } else if (text.contains("Русский")) {
                user.setLanguage("ru");
                user.setStatus(Status.WAITING_FOR_CHANNEL_VERIFICATION);
                UserService.update(user);
                return MessageService.createMessage(
                        chatId,
                        ResourceBundleManager.getMessage("ask_for_join", user.getLanguage()),
                        forceSubscribe(user)
                );
            }
        }
        
        // If no valid language selected, show language selection again
        return MessageService.createMessage(
                chatId,
                "Iltimos, quyidagi tillardan birini tanlang:\nПожалуйста, выберите один из языков:",
                languageKeyboard()
        );
    }
}
