package org.example.service;

import org.example.model.User;
import org.example.model.enums.Status;
import org.example.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.meta.api.objects.Message;

import java.util.List;

public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    public static User getByChatId( Message message) {
        logger.info("Fetching user for chatId: {}", message.getChatId());
        List<User> users = UserRepository.readUsers();

        User user = users.stream()
                .filter(u -> u.getId().equals(message.getChatId()))
                .findFirst()
                .orElse(null);

        if (user != null) {
            logger.info("Found existing user: {}", user.getUsername());
            return user;
        }

        logger.info("Creating new user for chatId: {}", message.getChatId());
        User newUser = createUser( message);
        users.add(newUser);
        UserRepository.saveUsers(users);
        logger.info("Saved new user: {}", newUser.getUsername());
        return newUser;
    }

    private static User createUser( Message message) {
        User newUser = new User();
        newUser.setId(message.getFrom().getId());
        newUser.setUsername(message.getFrom().getUserName());
        newUser.setStatus(Status.START);
        newUser.setAdmin(false);
        return newUser;
    }

    public static void update(User updatedUser) {
        logger.info("Updating user: {}", updatedUser.getUsername());
        List<User> users = UserRepository.readUsers();

        boolean found = false;
        for (int i = 0; i < users.size(); i++) {
            if (users.get(i).getId().equals(updatedUser.getId())) {
                users.set(i, updatedUser);
                found = true;
                break;
            }
        }

        if (!found) {
            logger.info("User not found, adding as new: {}", updatedUser.getUsername());
            users.add(updatedUser);
        }

        UserRepository.saveUsers(users);
        logger.info("User updated successfully: {}", updatedUser.getUsername());
    }
}