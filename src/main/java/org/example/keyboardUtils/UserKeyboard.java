package org.example.keyboardUtils;

import org.example.model.User;
import org.example.utils.ResourceBundleManager;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class UserKeyboard {
    public static ReplyKeyboardMarkup createUserKeyboard(User user) {
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();
        
        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(ResourceBundleManager.getMessage("button.send_ad",user.getLanguage()));
        firstRow.add(ResourceBundleManager.getMessage("button.ad_status",user.getLanguage()));
        
        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(ResourceBundleManager.getMessage("button.statistics",user.getLanguage()));
        
        // Add Back button in a new row
        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add(ResourceBundleManager.getMessage("button.back",user.getLanguage()));

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);

        
        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setOneTimeKeyboard(true);
        
        return keyboardMarkup;
    }
    public static ReplyKeyboardMarkup createStatisticsKeyboard(User user){
        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
        List<KeyboardRow> keyboard = new ArrayList<>();

        KeyboardRow firstRow = new KeyboardRow();
        firstRow.add(ResourceBundleManager.getMessage("button_ad_statistics",user.getLanguage()));

        KeyboardRow secondRow = new KeyboardRow();
        secondRow.add(ResourceBundleManager.getMessage("button_ad_price",user.getLanguage()));


        KeyboardRow thirdRow = new KeyboardRow();
        thirdRow.add(ResourceBundleManager.getMessage("button_back",user.getLanguage()));

        keyboard.add(firstRow);
        keyboard.add(secondRow);
        keyboard.add(thirdRow);

        keyboardMarkup.setKeyboard(keyboard);
        keyboardMarkup.setResizeKeyboard(true);
        keyboardMarkup.setSelective(true);
        keyboardMarkup.setOneTimeKeyboard(true);

        return keyboardMarkup;
    }

}
