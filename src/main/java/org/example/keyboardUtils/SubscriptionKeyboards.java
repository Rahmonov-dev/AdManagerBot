package org.example.keyboardUtils;

import org.example.model.User;
import org.example.utils.ResourceBundleManager;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubscriptionKeyboards {
    

    public static InlineKeyboardMarkup forceSubscribe(User user) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        InlineKeyboardButton channel1 = new InlineKeyboardButton();
        channel1.setText("📢 Test1 Channel");
        channel1.setUrl("https://t.me/test_for_my_bott");
        keyboard.add(Collections.singletonList(channel1));

        InlineKeyboardButton channel2 = new InlineKeyboardButton();
        channel2.setText("📢 Test2 Channel");
        channel2.setUrl("https://t.me/test2_for_mybot");
        keyboard.add(Collections.singletonList(channel2));

        InlineKeyboardButton channel3 = new InlineKeyboardButton();
        channel3.setText("📢 Test3 Channel");
        channel3.setUrl("https://t.me/test1_for_mybot");
        keyboard.add(Collections.singletonList(channel3));

         InlineKeyboardButton verifyButton = new InlineKeyboardButton();
        verifyButton.setText(ResourceBundleManager.getMessage("joined_verification",user));
        verifyButton.setCallbackData("verify_channels");
        keyboard.add(Collections.singletonList(verifyButton));

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
