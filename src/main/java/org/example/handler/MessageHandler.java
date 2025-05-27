package org.example.handler;

import org.example.keyboardUtils.LanguageKeyboards;
import org.example.keyboardUtils.SubscriptionKeyboards;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.UserService;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;

import static org.example.keyboardUtils.LanguageKeyboards.languageKeyboard;
import org.example.utils.ResourceBundleManager;


public class MessageHandler {



    public static BotApiMethod<?> handleMessage(Message message) {
        Long chatId = message.getChatId();
        String text = message.getText();

        User user = UserService.getByChatId(chatId, message);
        Status status = user.getStatus();
        if ("/start".equals(text)) {
            user.setStatus(Status.START);
            UserService.update(user);
            return handleStart(message, user);
        }
        switch (status) {
            case START:
                return handleStart(message, user);
            case WAITING_FOR_LANGUAGE_SELECTION:
                return handleLanguageSelection(message, user);
            case WAITING_FOR_CHANNEL_VERIFICATION:
                return handleChannelVerification(message, user);
            case WAITING_FOR_AD_TEXT:
                return handleAdText(message, user);
            case WAITING_FOR_MEDIA:
                return handleMedia(message, user);
            case WAITING_FOR_CONFIRMATION:
                return handleConfirmation(message, user);
            case WAITING_FOR_SCHEDULE_TIME:
                return handleScheduleTime(message, user);
            case WAITING_FOR_PAYMENT:
                return handlePayment(message, user);
            case SENDING_AD:
                return handleSendingAd(message, user);
            case FINISHED:
                return handleFinished(message, user);
            case CANCELLED:
                return handleCancelled(message, user);
            case ADMIN_PANEL:
                return handleAdminPanel(message, user);
            case UNKNOWN:
            default:
                return new SendMessage(chatId.toString(), "Nomaʼlum holat. /start buyrugʻini yuboring.");
        }
    }

    private static BotApiMethod<?> handleAdminPanel(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleCancelled(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleFinished(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handlePayment(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleSendingAd(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleScheduleTime(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleConfirmation(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleMedia(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleAdText(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleChannelVerification(Message message, User user) {
        return null;
    }

    private static BotApiMethod<?> handleLanguageSelection(Message message, User user) {
        String text = message.getText();
        Long chatId = message.getChatId();
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());

        boolean languageSelected = processLanguageSelection(text, user, sendMessage);
        if (!languageSelected) {
            // Show language selection prompt in both languages
            sendMessage.setText("Iltimos, quyidagi tillardan birini tanlang:\nПожалуйста, выберите один из языков:");
            sendMessage.setReplyMarkup(LanguageKeyboards.languageKeyboard());
        } else {
            // Set appropriate message based on selected language
            String messageText = "uz".equals(user.getLanguage()) ? 
                "Iltimos, quyidagi kanallarga a'zo bo'ling:" : 
                "Пожалуйста, подпишитесь на следующие каналы:";
            sendMessage.setText(messageText);
        }

        return sendMessage;
    }

    private static boolean processLanguageSelection(String text, User user, SendMessage sendMessage) {
        if (text == null) {
            return false;
        }

        String language = null;
        if (text.contains("O'zbekcha")) {
            language = "uz";
        } else if (text.contains("Русский")) {
            language = "ru";
        } else {
            return false;
        }

        user.setLanguage(language);
        user.setStatus(Status.WAITING_FOR_CHANNEL_VERIFICATION);
        UserService.update(user);
        sendMessage.setReplyMarkup(SubscriptionKeyboards.forceSubscribe());
        return true;
    }



    private static BotApiMethod<?> handleStart(Message message, User user) {
        Long chatId = message.getChatId();

        user.setStatus(Status.WAITING_FOR_LANGUAGE_SELECTION);
        UserService.update(user);

        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId.toString());
        
        String uzbekText = ResourceBundleManager.getMessage("select_language", "uz");
        String russianText = ResourceBundleManager.getMessage("select_language", "ru");
        sendMessage.setText(uzbekText + "\n" + russianText);
        sendMessage.setReplyMarkup(languageKeyboard());

        return sendMessage;
    }


}
