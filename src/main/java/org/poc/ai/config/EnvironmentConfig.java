package org.poc.ai.config;

import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Loads and provides access to environment variables from .env file
 */
public class EnvironmentConfig {
    private static final Logger logger = LoggerFactory.getLogger(EnvironmentConfig.class);
    private static Dotenv dotenv;
    
    static {
        try {
            dotenv = Dotenv.configure().load();
            logger.info("Environment variables loaded successfully");
        } catch (Exception e) {
            logger.warn("Failed to load .env file: {}", e.getMessage());
            logger.info("Will use system environment variables instead");
        }
    }
    
    /**
     * Gets an environment variable value
     * 
     * @param key the environment variable name
     * @return the value of the environment variable
     */
    public static String get(String key) {
        String value = dotenv != null ? dotenv.get(key) : System.getenv(key);
        if (value == null) {
            logger.warn("Environment variable {} not found", key);
        }
        return value;
    }
    
    /**
     * Gets an environment variable value with a default fallback
     * 
     * @param key the environment variable name
     * @param defaultValue the default value to return if the environment variable is not found
     * @return the value of the environment variable or the default value
     */
    public static String get(String key, String defaultValue) {
        String value = get(key);
        return value != null ? value : defaultValue;
    }
}
