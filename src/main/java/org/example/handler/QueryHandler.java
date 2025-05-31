package org.example.handler;

import org.example.TelegramBot;
import org.example.keyboardUtils.SubscriptionKeyboards;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.MessageService;
import org.example.service.UserService;
import org.example.utils.ResourceBundleManager;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;

import java.text.MessageFormat;
import java.util.List;

import static org.example.handler.MessageHandler.handleAdminPanel;


public class QueryHandler {


    public static BotApiMethod<?> handleQuery(CallbackQuery callbackQuery) {
        Message message = (Message) callbackQuery.getMessage();
        User user = UserService.getByChatId(message);
        Status status = user.getStatus();

        switch (status) {
            case WAITING_FOR_CHANNEL_VERIFICATION:
                return handleChannelVerification((Message) callbackQuery.getMessage(), user);
            default:
                return MessageService.createMessage(message.getChatId(), "Nomaʼlum holat. /start buyrugʻini yuboring.");
        }
    }

    private static BotApiMethod<?> handleChannelVerification(Message message, User user) {
        boolean isUserJoinedChannel = checkForSubscription(user);

        if (!isUserJoinedChannel) {
            String notSubscribedText = ResourceBundleManager.getMessage("not_subscribed", user.getLanguage());
            return MessageService.createMessage(
                message.getChatId(),
                notSubscribedText,
                SubscriptionKeyboards.forceSubscribe(user)
            );
        }

        if (user.isAdmin()) {
            user.setStatus(Status.ADMIN_PANEL);
            UserService.update(user);
            
            return handleAdminPanel(message, user);
        } else {
            user.setStatus(Status.USER_PANEL);
            UserService.update(user);
            
            String messageText = ResourceBundleManager.getMessage("user_panel", user.getLanguage());
            return MessageService.createMessage(
                message.getChatId(), 
                messageText,
                org.example.keyboardUtils.UserKeyboard.createUserKeyboard(user)
            );
        }
    }

    private static boolean checkForSubscription(User user) {
        List<String> channels = List.of("@test_for_my_bott", "@test2_for_mybot", "@test1_for_mybot");
        TelegramBot bot = new TelegramBot();
        for (String channel : channels) {
            try {
                GetChatMember check = new GetChatMember();
                check.setChatId(channel);
                check.setUserId(user.getId());

                ChatMember member = bot.execute(check);
                String status = member.getStatus();

                boolean isSubscribed = status.equals("member") || status.equals("administrator") || status.equals("creator");

                if (!isSubscribed) {
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Error checking subscription for user " + user.getId() + " in channel " + channel + ": " + e.getMessage());
                return false;
            }
        }
        return true;
    }

}
