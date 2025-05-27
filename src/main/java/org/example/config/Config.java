package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Config {
    private static final Logger logger = LoggerFactory.getLogger(Config.class);
    private static final Dotenv dotenv = Dotenv.configure().directory("src/main/resources/.env").ignoreIfMissing().load();

    public static String get(String key) {
        String value = dotenv.get(key);
        if (value == null) {
            logger.error("Configuration key not found: {}", key);
            throw new IllegalStateException("Configuration key not found: " + key);
        }
        logger.info("Loaded config key: {}", key);
        return value;
    }
}