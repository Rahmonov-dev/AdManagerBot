package org.example.handler.base;

import org.example.model.User;
import org.example.service.MessageService;
import org.example.utils.ResourceBundleManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public abstract class BaseHandler {
    protected static final Map<Long, Object> tempData = new ConcurrentHashMap<>();
    
    protected Object getErrorMessage(Long chatId, User user) {
        return MessageService.createMessage(chatId,
                ResourceBundleManager.getMessage("error.unknown_command", user.getLanguage()));
    }
    
    protected static <T> T getTempData(Long chatId, Class<T> clazz) {
        Object data = tempData.get(chatId);
        return clazz.isInstance(data) ? clazz.cast(data) : null;
    }
    
    protected static void setTempData(Long chatId, Object data) {
        tempData.put(chatId, data);
    }
    
    protected static void removeTempData(Long chatId) {
        tempData.remove(chatId);
    }
}
