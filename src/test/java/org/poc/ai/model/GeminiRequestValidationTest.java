package org.poc.ai.model;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for GeminiRequest validation
 */
class GeminiRequestValidationTest {
    
    private Validator validator;
    
    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }
    
    @Test
    @DisplayName("Valid request should pass validation")
    void validRequestShouldPassValidation() {
        // Create a valid request
        GeminiRequest request = GeminiRequest.createTextRequest("Test prompt");
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Valid request should not have validation errors");
    }
    
    @Test
    @DisplayName("Request with null contents should fail validation")
    void requestWithNullContentsShouldFailValidation() {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        request.setContents(null);
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty(), "Request with null contents should have validation errors");
        assertEquals(1, violations.size(), "Should have exactly one validation error");
        assertEquals("Contents cannot be empty", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("Request with empty contents should fail validation")
    void requestWithEmptyContentsShouldFailValidation() {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        request.setContents(new ArrayList<>());
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty(), "Request with empty contents should have validation errors");
        assertEquals(1, violations.size(), "Should have exactly one validation error");
        assertEquals("Contents cannot be empty", violations.iterator().next().getMessage());
    }
    
    @Test
    @DisplayName("Content with null parts should fail validation")
    void contentWithNullPartsShouldFailValidation() {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        content.setParts(null);
        content.setRole("user");
        request.setContents(List.of(content));
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty(), "Content with null parts should have validation errors");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Parts cannot be empty")),
                "Should have 'Parts cannot be empty' validation error");
    }
    
    @Test
    @DisplayName("Content with empty parts should fail validation")
    void contentWithEmptyPartsShouldFailValidation() {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        content.setParts(new ArrayList<>());
        content.setRole("user");
        request.setContents(List.of(content));
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty(), "Content with empty parts should have validation errors");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Parts cannot be empty")),
                "Should have 'Parts cannot be empty' validation error");
    }
    
    @Test
    @DisplayName("Part with null text should fail validation")
    void partWithNullTextShouldFailValidation() {
        // Create an invalid request
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        GeminiRequest.Part part = new GeminiRequest.Part();
        part.setText(null);
        content.setParts(List.of(part));
        content.setRole("user");
        request.setContents(List.of(content));
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty(), "Part with null text should have validation errors");
        assertTrue(violations.stream()
                .anyMatch(v -> v.getMessage().equals("Text cannot be null")),
                "Should have 'Text cannot be null' validation error");
    }
    
    @Test
    @DisplayName("Multiple validation errors should be reported")
    void multipleValidationErrorsShouldBeReported() {
        // Create an invalid request with multiple issues
        GeminiRequest request = new GeminiRequest();
        GeminiRequest.Content content = new GeminiRequest.Content();
        GeminiRequest.Part part = new GeminiRequest.Part();
        part.setText(null);
        content.setParts(List.of(part));
        
        GeminiRequest.Content emptyContent = new GeminiRequest.Content();
        emptyContent.setParts(new ArrayList<>());
        
        request.setContents(List.of(content, emptyContent));
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertFalse(violations.isEmpty(), "Request with multiple issues should have validation errors");
        assertTrue(violations.size() >= 2, "Should have at least two validation errors");
        
        // Check for specific error messages
        List<String> errorMessages = violations.stream()
                .map(ConstraintViolation::getMessage)
                .toList();
        
        assertTrue(errorMessages.contains("Text cannot be null"), 
                "Should contain 'Text cannot be null' validation error");
        assertTrue(errorMessages.contains("Parts cannot be empty"), 
                "Should contain 'Parts cannot be empty' validation error");
    }
    
    @Test
    @DisplayName("Complex valid request should pass validation")
    void complexValidRequestShouldPassValidation() {
        // Create a complex valid request
        GeminiRequest request = new GeminiRequest();
        
        // First content
        GeminiRequest.Content content1 = new GeminiRequest.Content();
        GeminiRequest.Part part1 = new GeminiRequest.Part();
        part1.setText("First message");
        content1.setParts(List.of(part1));
        content1.setRole("user");
        
        // Second content
        GeminiRequest.Content content2 = new GeminiRequest.Content();
        GeminiRequest.Part part2 = new GeminiRequest.Part();
        part2.setText("Second message");
        content2.setParts(List.of(part2));
        content2.setRole("assistant");
        
        // Add contents to request
        request.setContents(List.of(content1, content2));
        
        // Add generation config
        GeminiRequest.GenerationConfig config = new GeminiRequest.GenerationConfig();
        config.setTemperature(0.7);
        config.setMaxOutputTokens(100);
        request.setGenerationConfig(config);
        
        // Validate
        Set<ConstraintViolation<GeminiRequest>> violations = validator.validate(request);
        
        // Assert
        assertTrue(violations.isEmpty(), "Complex valid request should not have validation errors");
    }
}
