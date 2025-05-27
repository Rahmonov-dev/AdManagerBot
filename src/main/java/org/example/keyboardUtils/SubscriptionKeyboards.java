package org.example.keyboardUtils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Utility class for creating subscription-related keyboards
 */
public class SubscriptionKeyboards {
    
    /**
     * Creates a keyboard with channel subscription buttons for mandatory channels
     * @return InlineKeyboardMarkup with channel subscription buttons
     */
    public static InlineKeyboardMarkup forceSubscribe() {
        // Create the main keyboard
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();

        // First channel button
        InlineKeyboardButton channel1 = new InlineKeyboardButton();
        channel1.setText("📢 Test Channel");
        channel1.setUrl("https://t.me/test_for_my_bott");
        keyboard.add(Collections.singletonList(channel1));

        // Second channel button
        InlineKeyboardButton channel2 = new InlineKeyboardButton();
        channel2.setText("📢 Main Channel");
        channel2.setUrl("https://t.me/your_main_channel");
        keyboard.add(Collections.singletonList(channel2));

        // Verification button
        InlineKeyboardButton verifyButton = new InlineKeyboardButton();
        verifyButton.setText("✅ I've joined the channels");
        verifyButton.setCallbackData("verify_channels");
        keyboard.add(Collections.singletonList(verifyButton));

        keyboardMarkup.setKeyboard(keyboard);
        return keyboardMarkup;
    }
}
