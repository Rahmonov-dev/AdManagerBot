package org.example.utils;

import org.example.model.User;
import java.util.Locale;
import java.util.ResourceBundle;

public class ResourceBundleManager {
    private static final String BASE_NAME = "messages";
    
    public static String getMessage(String key, String language) {
        try {
            Locale locale = "ru".equals(language) ? new Locale("ru") : new Locale("uz");
            ResourceBundle bundle = ResourceBundle.getBundle(BASE_NAME, locale);
            return bundle.getString(key);
        } catch (Exception e) {
            return key;
        }
    }
    
    public static String getMessage(String key, User user) {
        String language = (user != null && user.getLanguage() != null) ? user.getLanguage() : "uz";
        return getMessage(key, language);
    }
}
