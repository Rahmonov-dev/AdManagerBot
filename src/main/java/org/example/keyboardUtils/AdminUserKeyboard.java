package org.example.keyboardUtils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for creating admin and user panel keyboard
 */
public class AdminUserKeyboard {
    
    /**
     * Creates a keyboard with options for admin and user panels
     * @return Configured ReplyKeyboardMarkup instance
     */
    public static ReplyKeyboardMarkup createAdminUserKeyboard() {
        // Create a keyboard row with two buttons
        KeyboardRow row = new KeyboardRow();
        row.add("👤 User panelga kirish");
        row.add("🧑‍💼 Admin panelga kirish");

        // Add the row to the keyboard
        List<KeyboardRow> keyboard = new ArrayList<>();
        keyboard.add(row);

        // Configure the keyboard markup
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }
}

