package org.poc.ai.controller;

import jakarta.validation.Valid;
import org.poc.ai.client.VertexAiClient;
import org.poc.ai.model.GeminiRequest;
import org.poc.ai.model.GeminiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * REST controller for handling Gemini requests
 */
@RestController
@RequestMapping("/api/gemini")
public class GeminiRestController {
    private static final Logger logger = LoggerFactory.getLogger(GeminiRestController.class);
    
    private final VertexAiClient vertexAiClient;
    
    @Autowired
    public GeminiRestController(VertexAiClient vertexAiClient) {
        this.vertexAiClient = vertexAiClient;
    }
    
    /**
     * Processes a Gemini request
     * 
     * @param request the Gemini request
     * @return the Gemini response
     */
    @PostMapping("/generate")
    public ResponseEntity<GeminiResponse> generateContent(@Valid @RequestBody GeminiRequest request) {
        try {
            logger.info("Processing Gemini request via REST controller");
            GeminiResponse response = vertexAiClient.generateContent(request);
            return ResponseEntity.ok(response);
        } catch (IOException e) {
            logger.error("Error processing Gemini request", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Exception handler for validation errors
     */
    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidationExceptions(
            org.springframework.web.bind.MethodArgumentNotValidException ex) {
        
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(error -> error.getDefaultMessage())
                .reduce("Validation errors: ", (acc, error) -> acc + "\n- " + error);
        
        logger.error("Validation error: {}", errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}
