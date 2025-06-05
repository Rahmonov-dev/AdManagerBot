package org.example.handler.advertisement;

import org.telegram.telegrambots.meta.api.objects.Message;
import org.example.model.User;

public interface StateHandler {
    Object handle(Message message, User user);
}
