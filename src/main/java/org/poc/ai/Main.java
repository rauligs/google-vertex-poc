package org.poc.ai;

import org.poc.ai.auth.GoogleAuthService;
import org.poc.ai.client.VertexAiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Main Spring Boot application class for the Vertex AI Gemini proxy
 */
@SpringBootApplication
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
        logger.info("Gemini Proxy API started successfully");
    }
    
    /**
     * Creates the GoogleAuthService bean
     */
    @Bean
    public GoogleAuthService googleAuthService() {
        return new GoogleAuthService();
    }
    
    /**
     * Creates the VertexAiClient bean
     */
    @Bean
    public VertexAiClient vertexAiClient(GoogleAuthService authService) {
        return new VertexAiClient(authService);
    }
}