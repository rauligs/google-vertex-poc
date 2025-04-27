package org.poc.ai;

import org.poc.ai.auth.GoogleAuthService;
import org.poc.ai.client.VertexAiClient;
import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main application class demonstrating how to use the Vertex AI client with Gemini Flash
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            // Initialize authentication service
            GoogleAuthService authService = new GoogleAuthService();
            
            // Initialize Vertex AI client
            VertexAiClient vertexAiClient = new VertexAiClient(authService);
            
            // Create a simple text request
            GeminiRequest request = GeminiRequest.createTextRequest("Tell me a short joke about programming");
            
            // Set generation parameters (optional)
            GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig();
            config.setTemperature(0.7);
            config.setMaxOutputTokens(100);
            request.setGenerationConfig(config);
            
            // Call the Gemini model
            logger.info("Sending request to Gemini Flash...");
            GeminiResponse response = vertexAiClient.generateContent(request);
            
            // Print the response
            String generatedText = response.getGeneratedText();
            if (generatedText != null) {
                logger.info("Generated response: {}", generatedText);
                System.out.println("\nGemini says: " + generatedText);
            } else {
                logger.warn("No text was generated in the response");
                System.out.println("No response was generated.");
            }
            
        } catch (IOException e) {
            logger.error("Error calling Gemini API: {}", e.getMessage(), e);
            System.err.println("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            System.err.println("Unexpected error: " + e.getMessage());
        }
    }
}