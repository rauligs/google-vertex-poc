package org.poc.ai.controller;

import com.google.gson.Gson;
import org.poc.ai.client.VertexAiClient;
import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Controller that acts as a proxy for Gemini requests to Vertex AI
 */
public class GeminiProxyController {
    private static final Logger logger = LoggerFactory.getLogger(GeminiProxyController.class);
    private static final Gson gson = new Gson();
    
    private final VertexAiClient vertexAiClient;
    
    public GeminiProxyController(VertexAiClient vertexAiClient) {
        this.vertexAiClient = vertexAiClient;
    }
    
    /**
     * Handles a Gemini request and forwards it to Vertex AI
     * 
     * @param inputStream the request input stream
     * @param outputStream the response output stream
     */
    public void handleRequest(InputStream inputStream, OutputStream outputStream) {
        try {
            // Parse Gemini request
            String requestBody = readInputStream(inputStream);
            GeminiRequest geminiRequest = gson.fromJson(requestBody, GeminiRequest.class);
            
            logger.info("Received Gemini request");
            
            // Validate the request
            validateRequest(geminiRequest);
            
            // Call Vertex AI
            GeminiResponse geminiResponse = vertexAiClient.generateContent(geminiRequest);
            
            // Write response
            writeResponse(outputStream, geminiResponse);
            
            logger.info("Successfully processed Gemini request");
        } catch (Exception e) {
            logger.error("Error processing Gemini request", e);
            writeErrorResponse(outputStream, e);
        }
    }
    
    /**
     * Processes a Gemini request and returns a Gemini response
     * 
     * @param geminiRequest the Gemini request
     * @return the Gemini response
     * @throws IOException if there's an error with the API call
     */
    public GeminiResponse processRequest(GeminiRequest geminiRequest) throws IOException {
        // Validate the request
        validateRequest(geminiRequest);
        
        // Call Vertex AI
        return vertexAiClient.generateContent(geminiRequest);
    }
    
    /**
     * Validates a Gemini request
     * 
     * @param request the request to validate
     * @throws IllegalArgumentException if the request is invalid
     */
    private void validateRequest(GeminiRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request cannot be null");
        }
        
        if (request.getContents() == null || request.getContents().isEmpty()) {
            throw new IllegalArgumentException("Request must contain at least one content item");
        }
        
        for (GeminiRequest.Content content : request.getContents()) {
            if (content.getParts() == null || content.getParts().isEmpty()) {
                throw new IllegalArgumentException("Each content item must contain at least one part");
            }
            
            for (GeminiRequest.Part part : content.getParts()) {
                if (part.getText() == null || part.getText().trim().isEmpty()) {
                    throw new IllegalArgumentException("Text parts cannot be empty");
                }
            }
        }
    }
    
    private String readInputStream(InputStream inputStream) {
        Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
    
    private void writeResponse(OutputStream outputStream, GeminiResponse response) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writer.write(gson.toJson(response));
            writer.flush();
        }
    }
    
    private void writeErrorResponse(OutputStream outputStream, Exception e) {
        try (PrintWriter writer = new PrintWriter(outputStream)) {
            writer.write("{\"error\": {\"message\": \"" + e.getMessage() + "\"}}");
            writer.flush();
        }
    }
}
