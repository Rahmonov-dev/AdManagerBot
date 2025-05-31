package org.example.keyboardUtils;


import org.example.model.User;
import org.example.utils.ResourceBundleManager;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class AdminUserKeyboard {
    public static ReplyKeyboardMarkup createAdminUserKeyboard(User user) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(ResourceBundleManager.getMessage("button.user_panel",user.getLanguage()));
        firstRow.add(ResourceBundleManager.getMessage("button.admin_panel",user.getLanguage()));
        
        keyboard.add(firstRow);
        keyboardMarkup.setKeyboard(keyboard);
        
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        
        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup createAdminKeyboard(User user) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        // First row
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(ResourceBundleManager.getMessage("button.approve_ad",user.getLanguage()));
        firstRow.add(ResourceBundleManager.getMessage("button.approve_payment",user.getLanguage()));

        // Second row
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(ResourceBundleManager.getMessage("button.ad_statistics",user.getLanguage()));
        secondRow.add(ResourceBundleManager.getMessage("button.user_control",user.getLanguage()));

        // Third row
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add(ResourceBundleManager.getMessage("button.menu",user.getLanguage()));

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setOneTimeKeyboard(false);

        return keyboardMarkup;
    }
}

