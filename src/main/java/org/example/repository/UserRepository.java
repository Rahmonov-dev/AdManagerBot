package org.example.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Synchronized;
import org.example.config.Config;
import org.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(UserRepository.class);
    private static final String FILE_NAME = Config.get("BOT_USER_JSON");
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    @Synchronized
    public static void saveUsers(List<User> users) {
        if (users == null) {
            logger.warn("Attempted to save null user list to file: {}", FILE_NAME);
            return;
        }
        
        File file = new File(FILE_NAME);
        try {
            logger.info("Saving {} users to file: {}", users.size(), file.getAbsolutePath());
            
            // Ensure parent directory exists
            File parentDir = file.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                logger.info("Creating parent directory: {}", parentDir.getAbsolutePath());
                if (!parentDir.mkdirs()) {
                    logger.error("Failed to create parent directory: {}", parentDir.getAbsolutePath());
                }
            }
            
            // Create file if it doesn't exist
            if (!file.exists()) {
                logger.info("Creating new file: {}", file.getAbsolutePath());
                if (!file.createNewFile()) {
                    logger.error("Failed to create file: {}", file.getAbsolutePath());
                }
            }
            
            // Write to a temporary file first
            File tempFile = new File(file.getAbsolutePath() + ".tmp");
            objectMapper.writeValue(tempFile, users);
            
            // Replace the original file with the temporary file
            if (file.exists() && !file.delete()) {
                logger.warn("Could not delete original file before rename");
            }
            if (!tempFile.renameTo(file)) {
                throw new IOException("Failed to rename temporary file to " + file.getName());
            }
            
            logger.info("Successfully saved {} users to file: {}", users.size(), file.getAbsolutePath());
        } catch (IOException e) {
            logger.error("Failed to save users to file: " + file.getAbsolutePath(), e);
            // Don't throw exception to prevent application crash
        }
    }

    @Synchronized
    public static List<User> readUsers() {
        File file = new File(FILE_NAME);
        
        // If file doesn't exist, create it with empty array
        if (!file.exists()) {
            logger.info("User file does not exist: {}. Creating new file with empty array.", FILE_NAME);
            List<User> emptyList = new ArrayList<>();
            saveUsers(emptyList);
            return emptyList;
        }
        
        // If file is empty, initialize it with empty array
        if (file.length() == 0) {
            logger.info("User file is empty: {}. Initializing with empty array.", FILE_NAME);
            List<User> emptyList = new ArrayList<>();
            saveUsers(emptyList);
            return emptyList;
        }
        
        try {
            logger.info("Reading users from file: {}", FILE_NAME);
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            if (content.trim().isEmpty()) {
                logger.info("File is empty, initializing with empty array");
                List<User> emptyList = new ArrayList<>();
                saveUsers(emptyList);
                return emptyList;
            }
            
            List<User> users = objectMapper.readValue(file, new TypeReference<List<User>>() {});
            logger.info("Successfully read {} users from file: {}", users.size(), FILE_NAME);
            return users != null ? users : new ArrayList<>();
        } catch (IOException e) {
            logger.error("Failed to read users from file: {}", FILE_NAME, e);
            // If there's an error reading, return empty list instead of crashing
            return new ArrayList<>();
        }
    }
}