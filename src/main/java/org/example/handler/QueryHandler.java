package org.example.handler;

import lombok.Setter;
import org.example.TelegramBot;
import org.example.keyboardUtils.SubscriptionKeyboards;
import org.example.model.User;
import org.example.model.enums.Status;
import org.example.service.MessageService;
import org.example.service.UserService;
import org.example.utils.ResourceBundleManager;

import org.telegram.telegrambots.meta.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.chatmember.ChatMember;


import java.util.List;

import static org.example.keyboardUtils.AdminUserKeyboard.createAdminUserKeyboard;


public class QueryHandler {
    @Setter
    private static TelegramBot bot;

    @SuppressWarnings("rawtypes")
    public static Object handleQuery(CallbackQuery callbackQuery) {
        Message message = (Message) callbackQuery.getMessage();
        User user = UserService.getByChatId(message);
        String callbackData = callbackQuery.getData();
        
        // Handle verify_channels callback
        if ("verify_channels".equals(callbackData)) {
            return handleChannelVerification(message, user);
        }
        
        // Default response for unknown callbacks
        return MessageService.createMessage(message.getChatId(), 
            ResourceBundleManager.getMessage("unknown_command", user.getLanguage()));
    }

    private static Object handleChannelVerification(Message message, User user) {
        Long chatId = message.getChatId();
        String language = user.getLanguage() != null ? user.getLanguage() : "uz";
        
        try {
            System.out.println("Verifying channel subscriptions for user: " + user.getId());
            boolean isUserJoinedChannel = checkForSubscription(user);

            if (!isUserJoinedChannel) {
                String notSubscribedText = ResourceBundleManager.getMessage("not_subscribed", language);
                System.out.println("User " + user.getId() + " is not subscribed to all required channels");
                
                // Edit the existing message instead of sending a new one
                return MessageService.createMessage(
                    chatId,
                    notSubscribedText,
                    SubscriptionKeyboards.forceSubscribe(user)
                );
            }

            // User is subscribed to all channels, proceed to appropriate panel
            if (user.isAdmin()) {
                user.setStatus(Status.ADMIN_PANEL);
                UserService.update(user);
                
                String welcomeMessage = ResourceBundleManager.getMessage("welcome", language) + "\n\n" +
                                      ResourceBundleManager.getMessage("admin_panel", language);
                
                return MessageService.createMessage(
                    chatId,
                    welcomeMessage,
                    createAdminUserKeyboard(user)
                );
            } else {
                user.setStatus(Status.USER_PANEL);
                UserService.update(user);
                
                String welcomeMessage = ResourceBundleManager.getMessage("welcome", language) + "\n\n" +
                                      ResourceBundleManager.getMessage("user_panel", language);
                
                return MessageService.createMessage(
                    chatId,
                    welcomeMessage,
                    org.example.keyboardUtils.UserKeyboard.createUserKeyboard(user)
                );
            }
        } catch (Exception e) {
            System.err.println("Error in handleChannelVerification: " + e.getMessage());
            e.printStackTrace();
            return MessageService.createMessage(
                chatId,
                ResourceBundleManager.getMessage("error_occurred", language) + "\n" +
                ResourceBundleManager.getMessage("try_again", language)
            );
        }
    }

    private static boolean checkForSubscription(User user) {
        // List of channels to check (should match the ones in SubscriptionKeyboards.forceSubscribe)
        List<String> channels = List.of("@test_for_my_bott", "@test2_for_mybot", "@test1_for_mybot");
        
        // If bot is not initialized, log error and return false
        if (bot == null) {
            System.err.println("Error: Bot instance is not initialized in QueryHandler");
            return false;
        }

        for (String channel : channels) {
            try {
                System.out.println("Checking subscription for user " + user.getId() + " in channel " + channel);
                
                // Create and execute GetChatMember request
                GetChatMember getChatMember = new GetChatMember();
                getChatMember.setChatId(channel);
                getChatMember.setUserId(user.getId());
                
                ChatMember member = bot.execute(getChatMember);
                String status = member.getStatus().toLowerCase();

                // Check if user is subscribed (member, admin, or creator)
                boolean isSubscribed = status.equals("member") || 
                                      status.equals("administrator") || 
                                      status.equals("creator");

                System.out.println("User " + user.getId() + " in channel " + channel + ": " + status + " (subscribed: " + isSubscribed + ")");
                
                if (!isSubscribed) {
                    System.out.println("User " + user.getId() + " is not subscribed to " + channel);
                    return false;
                }
            } catch (Exception e) {
                System.err.println("Error checking subscription for user " + user.getId() + " in channel " + channel + ": " + e.getMessage());
                e.printStackTrace();
                return false;
            }
        }
        
        System.out.println("User " + user.getId() + " is subscribed to all required channels");
        return true;
    }

}
